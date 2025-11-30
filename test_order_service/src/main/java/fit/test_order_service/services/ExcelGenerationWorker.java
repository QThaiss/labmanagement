/*
 * @ (#) ExcelGenerationWorker.java    1.0    23/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.test_order_service.client.IamFeignClient;
import fit.test_order_service.client.dtos.UserInternalResponse;
import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.entities.ReportFileStore;
import fit.test_order_service.entities.ReportJob;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.enums.JobStatus;
import fit.test_order_service.repositories.ReportFileStoreRepository;
import fit.test_order_service.repositories.ReportJobRepository;
import fit.test_order_service.repositories.TestOrderRepository;
import fit.test_order_service.utils.ExcelGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelGenerationWorker {

    private final ReportJobRepository reportJobRepository;
    private final TestOrderRepository testOrderRepository;
    private final ReportFileStoreRepository fileStoreRepository;
    private final ObjectMapper objectMapper;
    private final ExcelGeneratorUtil excelGeneratorUtil;
    private final FileStorageService fileStorageService;
    private final IamFeignClient iamFeignClient;

    @Value("${app.cloudinary.export-folder}")
    private String excelFolder;

    // Đưa MIME type ra làm hằng số
    private static final String EXCEL_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // Hàm bỏ dấu
    private static String removeAccents(String text) {
        if (text == null || text.isBlank()) return text;
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccents = pattern.matcher(normalized).replaceAll("");
        return noAccents.replace('đ', 'd').replace('Đ', 'D');
    }


    @Async
    @Transactional
    public void generateExcel(String jobId) {
        log.info("Starting Excel generation for job ID: {}", jobId);
        ReportJob job = reportJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Job ID {} not found.", jobId);
            return;
        }

        List<String> targetOrderIds = Collections.emptyList();
        String customFileName;
        String dateRangeType;
        LocalDate startDate = null;
        LocalDate endDate = null;
        List<TestOrder> ordersToExport;


        try {
            // 0. Parse Params
            if (job.getParamsJson() != null && !job.getParamsJson().isBlank()) {
                try {
                    Map<String, Object> params = objectMapper.readValue(job.getParamsJson(), new TypeReference<>() {
                    });
                    if (params.containsKey("orderIds")) {
                        Object idsObj = params.get("orderIds");
                        if (idsObj instanceof List) {
                            targetOrderIds = ((List<?>) idsObj).stream().map(Object::toString).toList();
                        }
                    }
                    customFileName = (String) params.get("customFileName");

                    // Lấy các tham số thời gian
                    dateRangeType = (String) params.get("dateRangeType");
                    if (params.containsKey("startDate")) {
                        startDate = LocalDate.parse((String) params.get("startDate"), DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                    if (params.containsKey("endDate")) {
                        endDate = LocalDate.parse((String) params.get("endDate"), DateTimeFormatter.ISO_LOCAL_DATE);
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Could not parse params JSON for job " + jobId, e);
                }
            } else {
                throw new RuntimeException("Job params JSON is missing for job " + jobId);
            }


            // 1. Cập nhật trạng thái Job -> RUNNING
            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now(ZoneOffset.UTC));
            job.setProgressPct(10);
            reportJobRepository.save(job);


            // 2. Lấy danh sách Test Orders cần export
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            boolean queryByDate = false; // <-- Khởi tạo mặc định là false

            if (!targetOrderIds.isEmpty()) {
                // Ưu tiên query theo ID list
                log.info("Querying orders by specific ID list ({} IDs).", targetOrderIds.size());
                ordersToExport = testOrderRepository.findByOrderIdInAndDeletedFalse(targetOrderIds);
            } else if (dateRangeType != null) {
                // Nếu không có ID list, query theo date range
                queryByDate = true;
                LocalDate today = LocalDate.now(ZoneOffset.UTC); // Lấy ngày hiện tại theo UTC

                switch (dateRangeType) {
                    case "TODAY":
                        startDateTime = today.atStartOfDay();
                        endDateTime = today.atTime(23, 59, 59, 999999999);
                        break;
                    case "THIS_MONTH":
                        YearMonth currentMonth = YearMonth.from(today);
                        startDateTime = currentMonth.atDay(1).atStartOfDay();
                        endDateTime = currentMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);
                        break;
                    case "THIS_YEAR":
                        Year currentYear = Year.from(today);
                        startDateTime = currentYear.atDay(1).atStartOfDay();
                        endDateTime = LocalDate.of(currentYear.getValue(), 12, 31).atTime(23, 59, 59, 999999999);
                        break;
                    case "CUSTOM":
                        if (startDate != null && endDate != null) {
                            startDateTime = startDate.atStartOfDay();
                            endDateTime = endDate.atTime(23, 59, 59, 999999999);
                        } else {
                            // Lỗi logic nếu vào đây (validation nên bắt ở service)
                            throw new IllegalStateException("CUSTOM date range requested but startDate or endDate is missing in params for job " + jobId);
                        }
                        break;
                    case "ALL_TIME":
                    default: // Mặc định hoặc ALL_TIME sẽ lấy tất cả
                        queryByDate = false; // Đặt lại cờ, sẽ query không theo ngày
                        break;
                }

                if (queryByDate) {
                    log.info("Querying orders using date range [{}]: {} to {}", dateRangeType, startDateTime, endDateTime);
                    ordersToExport = testOrderRepository.findByDeletedFalseAndCreatedAtBetween(startDateTime, endDateTime);
                } else { // Trường hợp ALL_TIME
                    log.info("Querying ALL non-deleted orders.");
                    ordersToExport = testOrderRepository.findByDeletedFalse();
                }

            } else {
                // Không có ID list và không có dateRangeType
                log.warn("No orderIds provided and no dateRangeType specified for job {}. Exporting all non-deleted orders (default).", jobId);
                // Mặc định là ALL_TIME
                ordersToExport = testOrderRepository.findByDeletedFalse();
            }


            // Lọc bỏ SYSTEM_ORDER_ID
            ordersToExport = ordersToExport.stream()
                    .filter(order -> !"SYSTEM_ORDER_ID".equals(order.getOrderId()))
                    .collect(Collectors.toList());
            log.info("Filtered list size after removing SYSTEM_ORDER_ID: {}", ordersToExport.size());


            // Kiểm tra danh sách rỗng sau khi lọc
            if (ordersToExport.isEmpty()) {
                log.warn("No valid Test Orders found to export (after filtering) for job ID: {}", jobId);
                job.setStatus(JobStatus.SUCCEEDED);
                job.setMessage("No test orders found matching the criteria (after filtering).");
                job.setProgressPct(100);
                job.setFinishedAt(LocalDateTime.now(ZoneOffset.UTC));
                reportJobRepository.save(job);
                return;
            }

            job.setProgressPct(30);
            reportJobRepository.save(job);


            // 3. Tạo file Excel
            List<String> userIds = ordersToExport.stream()
                    .flatMap(o -> Stream.of(o.getCreatedBy(), o.getRunBy()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Map<String, String> userIdToNameMap = fetchUserNames(userIds);

            ByteArrayOutputStream excelStream = excelGeneratorUtil.generateTestOrdersExcel(ordersToExport, userIdToNameMap);
            byte[] excelBytes = excelStream.toByteArray();

            if (excelBytes.length == 0) {
                throw new RuntimeException("Generated Excel is empty.");
            }
            log.info("Generated excelBytes length: {} bytes", excelBytes.length);

            job.setProgressPct(70);
            reportJobRepository.save(job);

            String fileName = determineFileName(customFileName);

            // Cập nhật lời gọi: truyền 'null' cho 'requestedDirectoryPath' (tham số thứ 2)
            String fileKey = fileStorageService.storeFile(
                    excelBytes,
                    fileName,
                    excelFolder,
                    EXCEL_MIME_TYPE,
                    job.getRequestedBy()
            );

            // 'fileKey' bây giờ là URL Cloudinary
            ReportFileStore fileStore = ReportFileStore.builder()
                    .storageType(fileStorageService.getStorageType())
                    .objectKey(fileKey) // Lưu URL vào objectKey
                    .fileName(fileName)
                    .mimeType(EXCEL_MIME_TYPE) // <-- Dùng hằng số
                    .byteSize((long) excelBytes.length)
                    .createdBy(job.getRequestedBy())
                    .build();
            ReportFileStore savedFileStore = fileStoreRepository.save(fileStore);

            job.setResultFileId(savedFileStore.getFileId());
            job.setResultFile(savedFileStore);
            job.setStatus(JobStatus.SUCCEEDED);
            job.setMessage("Excel file generated successfully: " + fileKey); // Message chứa URL
            job.setProgressPct(100);
            job.setFinishedAt(LocalDateTime.now(ZoneOffset.UTC));
            reportJobRepository.save(job);

            log.info("Successfully generated Excel for job ID: {}. File ID: {}. Location (URL): {}", jobId, savedFileStore.getFileId(), fileKey);

        } catch (Exception e) {
            log.error("Failed to generate Excel for job ID: {}", jobId, e);
            // Cập nhật job là FAILED
            job.setStatus(JobStatus.FAILED);
            job.setMessage("Error during Excel generation: " + e.getMessage());
            job.setFinishedAt(LocalDateTime.now(ZoneOffset.UTC));
            job.setProgressPct(job.getProgressPct()); // Giữ % progress lúc xảy ra lỗi
            reportJobRepository.save(job);
        }
    }

    // Helper để lấy tên user
    private Map<String, String> fetchUserNames(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> nameMap = new HashMap<>();
        for (String userId : userIds) {
            if (userId == null || userId.isBlank() || "SYSTEM".equals(userId)) {
                nameMap.put(userId, userId);
                continue;
            }
            try {
                ApiResponse<UserInternalResponse> response = iamFeignClient.getUserById(userId);
                if (response != null && response.getData() != null && response.getData().fullName() != null) {
                    nameMap.put(userId, response.getData().fullName());
                } else {
                    nameMap.put(userId, userId + " (Name not found)");
                }
            } catch (Exception e) {
                if (!userId.equals("INSTRUMENT_HL7_INGEST")) {
                    log.error("Error fetching name for user ID {}: {}", userId, e.getMessage());
                    nameMap.put(userId, userId + " (Error)");
                } else {
                    nameMap.put(userId, userId);
                }

            }
        }
        return nameMap;

        // Ghi chú: Logic batch tối ưu hơn, nhưng logic lặp hiện tại vẫn đúng.
    }


    // Cập nhật để dùng default mới
    private String determineFileName(String customFileName) {
        String finalBaseName;

        if (customFileName != null && !customFileName.isBlank()) {
            finalBaseName = removeAccents(customFileName);
        } else {
            // Tên mặc định MỚI: DanhSachPhieuXetNghiem-<DateExport>
            String dateExport = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            finalBaseName = String.format("DanhSachPhieuXetNghiem-%s", dateExport);
        }

        String sanitizedName = finalBaseName
                .replaceAll("[^a-zA-Z0-9\\s\\-_.]+", "_")
                .replace(" ", "_");

        // Đảm bảo có đuôi .xlsx
        return sanitizedName.toLowerCase().endsWith(".xlsx") ? sanitizedName : sanitizedName + ".xlsx";
    }
}