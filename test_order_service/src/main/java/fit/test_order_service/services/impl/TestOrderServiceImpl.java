package fit.test_order_service.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.test_order_service.client.IamFeignClient;
import fit.test_order_service.client.PatientMedicalRecordFeignClient;
import fit.test_order_service.client.WarehouseFeignClient;
import fit.test_order_service.client.dtos.PatientMedicalRecordInternalResponse;
import fit.test_order_service.client.dtos.UserInternalResponse;
import fit.test_order_service.dtos.event.SystemEvent;
import fit.test_order_service.dtos.request.*;
import fit.test_order_service.dtos.response.*;
import fit.test_order_service.entities.*;
import fit.test_order_service.enums.*;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.exceptions.NotFoundException;
import fit.test_order_service.exceptions.UnauthorizedException;
import fit.test_order_service.mappers.TestOrderMapper;
import fit.test_order_service.repositories.*;
import fit.test_order_service.services.*;
import fit.test_order_service.specifications.TestOrderSpecification;
import fit.test_order_service.utils.SecurityUtils;
import fit.test_order_service.utils.SortFields;
import fit.test_order_service.utils.SortUtils;
import fit.test_order_service.utils.TestOrderGenerator;
import fit.test_order_service.validators.TestOrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestOrderServiceImpl implements TestOrderService {
    private final TestOrderRepository testOrderRepository;
    private final TestOrderMapper testOrderMapper;
    private final PatientMedicalRecordFeignClient patientMedicalRecordFeignClient;
    private final OrderEventLogService orderEventLogService;
    private final TestOrderValidator testOrderValidator;
    private final IamFeignClient iamFeignClient;
    private final TestOrderSpecification testOrderSpecification;

    private final TestCatalogRepository testCatalogRepository;
    private final ReportJobRepository reportJobRepository;
    private final PdfGenerationQueueService pdfGenerationQueueService;
    private final ExcelGenerationQueueService excelGenerationQueueService;
    private final ObjectMapper objectMapper;
    private final TestOrderStatusService testOrderStatusService;
    private final TestResultAdjustLogRepository testResultAdjustLogRepository;

    private final TestResultRepository testResultRepository;

    private final OrderCommentRepository orderCommentRepository;
    private final Hl7ParserService hl7ParserService;

    private final TestTypeRepository testTypeRepository;
    private final TestTypeService testTypeService;
    private final WarehouseFeignClient warehouseFeignClient;

    private final EventLogPublisher eventLogPublisher;

    @Override
    @Transactional
    public TestOrderResponse createTestOrder(CreateTestOrderRequest request) {
        // 1. Lấy thông tin TestType để biết cần trừ hóa chất gì
        TestType testType = testTypeRepository.findById(request.getTestTypeId())
                .orElseThrow(() -> new NotFoundException("TestType not found with ID: " + request.getTestTypeId()));

        // 3. Lấy thông tin bệnh nhân (Logic cũ)
        ApiResponse<PatientMedicalRecordInternalResponse> patientApiResponse = patientMedicalRecordFeignClient.getPatientMedicalRecordByCode(request.getMedicalRecordCode());
        PatientMedicalRecordInternalResponse patientData = patientApiResponse.getData();

        // 4. Dùng mapper để chuyển đổi
        TestOrder testOrder = testOrderMapper.toEntity(patientData);
        if (testOrder == null) {
            throw new RuntimeException("Could not map patient data to test order.");
        }

        TestTypeResponse testTypeResponse = testTypeService.getTestTypeById(request.getTestTypeId());

        // 5. Set các thông tin còn lại
        testOrder.setTestTypeRef(testType); // QUAN TRỌNG: Gán TestType cho Order
        // Lưu snapshot các thông tin quan trọng của TestType tại thời điểm tạo đơn
        testOrder.setTestTypeIdSnapshot(testType.getId());
        testOrder.setTestTypeNameSnapshot(testType.getName());

        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        testOrder.setBarcode(TestOrderGenerator.generateBarcode());

        // 6. Lấy User ID từ SecurityUtils
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("Cannot create test order without a logged-in user.");
        }
        testOrder.setCreatedBy(currentUserId);

        // 7. Lưu và trả về
        TestOrder savedTestOrder = testOrderRepository.save(testOrder);

        // 8. Ghi log sự kiện CREATED
        orderEventLogService.logEvent(savedTestOrder, EventType.CREATE, "Test order created for medical record: " + savedTestOrder.getMedicalRecordCode());

        eventLogPublisher.publishEvent(SystemEvent.builder()
                .eventCode("E_00001")
                .action("Create Test Order")
                .message("Created test order " + savedTestOrder.getOrderId())
                .sourceService("TEST_ORDER_SERVICE")
                .operator(SecurityUtils.getCurrentUserId()) // Hàm lấy user hiện tại
                .details(Map.of("testOrderId", savedTestOrder.getOrderId(), "medicalRecordId", savedTestOrder.getMedicalRecordId()))
                .build());

        return testOrderMapper.toResponse(savedTestOrder, testTypeResponse);
    }

    @Override
    public TestOrderDetailResponse getTestOrderById(String id) {
        if ("SYSTEM_ORDER_ID".equals(id)) {
            throw new NotFoundException("Invalid request: SYSTEM_ORDER is internal only");
        }

        TestOrder testOrder = testOrderRepository.findByOrderIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Test order not found with id: " + id)); // Sửa lỗi cú pháp ở đây

        // Ghi log sự kiện VIEWED
        orderEventLogService.logEvent(testOrder, EventType.VIEW, "Viewed details for test order ID: " + id);

        // 1. Map thông tin cơ bản của Order
        TestOrderDetailResponse response = testOrderMapper.toDetailResponse(testOrder);

        if (testOrder.getStatus() == OrderStatus.COMPLETED) {
            response.setResults(testOrderMapper.toResultResponseList(testOrder.getResults()));
        }

        // --- BẮT ĐẦU LOGIC LẤY COMMENT (MAP THỦ CÔNG) ---

        // 2. Lấy ID của tất cả TestResult thuộc TestOrder này
        List<String> resultIds = testOrder.getResults()
                .stream()
                .map(TestResult::getResultId)
                .collect(Collectors.toList());

        // 3. Lấy top-level comments cho ORDER
        List<OrderComment> orderComments = orderCommentRepository
                .findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtAsc(
                        CommentTargetType.ORDER,
                        testOrder.getOrderId()
                );

        // 4. Lấy top-level comments cho RESULTS
        List<OrderComment> resultComments = new ArrayList<>();
        if (!resultIds.isEmpty()) {
            resultComments = orderCommentRepository
                    .findByTargetTypeAndTargetIdInAndParentIdIsNullOrderByCreatedAtAsc(
                            CommentTargetType.RESULT,
                            resultIds
                    );
        }

        // 5. Gộp, sắp xếp, và map thủ công sang DTO mới của bạn
        List<CommentOrderResponse> combinedComments = Stream.concat(
                        orderComments.stream(),
                        resultComments.stream()
                )
                .sorted(Comparator.comparing(OrderComment::getCreatedAt))
                .map(this::mapToCommentOrderResponse) // <-- Gọi hàm map thủ công
                .collect(Collectors.toList());

        // 6. Set vào response
        response.setComments(combinedComments);

        // --- KẾT THÚC LOGIC LẤY COMMENT ---

        return response;
    }

    @Override
    public TestOrderResponse getTestOrderByTestOrderId(String id) {
        TestOrder testOrder = testOrderRepository.findByOrderIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Test order not found with id: " + id));

        TestTypeResponse testTypeResponse = testTypeService.getTestTypeById(testOrder.getTestTypeRef().getId());

        return testOrderMapper.toResponse(testOrder, testTypeResponse);
    }

    // --- CÁC HÀM HELPER MAP THỦ CÔNG CHO getTestOrderById ---

    /**
     * Map thủ công từ Entity (OrderComment) sang DTO mới (CommentOrderResponse)
     */
    private CommentOrderResponse mapToCommentOrderResponse(OrderComment comment) {
        if (comment == null) {
            return null;
        }

        // Map các replies trước (đệ quy)
        List<CommentOrderResponse> replyDTOs = mapReplies(comment.getReplies());

        return CommentOrderResponse.builder()
                .id(comment.getCommentId())
                .author(mapAuthor(comment.getAuthorUserId())) // Gọi helper map author
                .targetInfo(mapTarget(comment.getTargetType(), comment.getTargetId())) // Gọi helper map target
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .edited(comment.isEdited())
                .updatedAt(comment.getUpdatedAt())
                .replies(replyDTOs)
                .build();
    }

    /**
     * Map thủ công danh sách replies (Đệ quy)
     */
    private List<CommentOrderResponse> mapReplies(List<OrderComment> replies) {
        if (replies == null || replies.isEmpty()) {
            return Collections.emptyList();
        }
        return replies.stream()
                .map(this::mapToCommentOrderResponse) // Gọi lại hàm map chính
                .collect(Collectors.toList());
    }

    /**
     * Map thủ công thông tin Author (Sử dụng CommentAuthorResponse DTO)
     */
    private CommentAuthorResponse mapAuthor(String authorUserId) {
        if (authorUserId == null) {
            return new CommentAuthorResponse("SYSTEM", "Hệ thống", List.of("SYSTEM"));
        }
        try {
            ApiResponse<UserInternalResponse> apiResponse = iamFeignClient.getUserById(authorUserId);
            UserInternalResponse userData = (apiResponse != null) ? apiResponse.getData() : null;

            if (userData != null) {
                return CommentAuthorResponse.builder()
                        .id(userData.userId())
                        .fullName(userData.fullName())
                        .roles(userData.roleName() != null ? List.of(userData.roleName()) : List.of("UNKNOWN"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to fetch user data from IAM for userId: {}. Error: {}", authorUserId, e.getMessage());
        }
        return CommentAuthorResponse.builder()
                .id(authorUserId)
                .fullName("Người dùng không xác định")
                .roles(List.of("UNKNOWN"))
                .build();
    }

    /**
     * Map thủ công thông tin Target (Sử dụng CommentResponse DTO)
     */
    private CommentResponse mapTarget(CommentTargetType type, String targetId) {
        CommentResponse.CommentResponseBuilder builder = CommentResponse.builder()
                .targetType(type);

        if (type == CommentTargetType.ORDER) {
            builder.testOrderId(targetId);

        } else if (type == CommentTargetType.RESULT) {
            builder.resultId(targetId);
            try {
                Optional<TestResult> resultOpt = testResultRepository.findById(targetId);
                if (resultOpt.isPresent()) {
                    TestResult result = resultOpt.get();

                    builder.testOrderId(result.getOrderId());
                    builder.analyteName(result.getAnalyteName());
                    builder.resultValue(result.getValueText());
                    builder.testName(result.getAnalyteName());
                    builder.testCode(result.getTestCode());
                }
            } catch (Exception e) {
                log.error("Failed to fetch target info for resultId: {}. Error: {}", targetId, e.getMessage());
            }
        }
        return builder.build();
    }

    @Override
    @Transactional
    public void deleteTestOrder(String id) {
        // 1. Tìm TestOrder, đảm bảo nó tồn tại và chưa bị xóa
        TestOrder testOrder = testOrderRepository.findByOrderIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Test order not found with id: " + id + " or it has already been deleted."));

        // Kiểm tra trạng thái
        if (testOrder.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new IllegalStateException("Cannot delete test order when status is COMPLETED");
        }

        // 2. Đánh dấu là đã xóa (soft-delete)
        testOrder.setDeleted(true);
        testOrder.setDeletedBy(SecurityUtils.getCurrentUserId());
        testOrder.setDeletedAt(LocalDateTime.now(ZoneOffset.UTC));

        // 4. Lưu lại thay đổi của TestOrder
        testOrderRepository.save(testOrder);

        eventLogPublisher.publishEvent(SystemEvent.builder()
                .eventCode("E_00003")
                .action("Delete Test Order")
                .message("Deleted test order " + id)
                .sourceService("TEST_ORDER_SERVICE")
                .operator(SecurityUtils.getCurrentUserId())
                .details(Map.of("orderId", id))
                .build());

        // 5. Ghi lại sự kiện DELETED vào log
        orderEventLogService.logEvent(testOrder, EventType.DELETE, "Test order with ID: " + id + " has been deleted.");
    }

    @Override
    @Transactional
    public TestOrderResponse updateTestOrderByCode(String orderCode, UpdateTestOrderRequest request) {
        // Validate and fetch existing order
        TestOrder existingOrder = testOrderValidator.validateForUpdate(orderCode);

        boolean isAutoCreated = existingOrder.isAutoCreated();

        // Clone before update (snapshot)
        TestOrder beforeUpdate = new TestOrder();
        BeanUtils.copyProperties(existingOrder, beforeUpdate);

        boolean hasChanges = false;

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            String newVal = request.getFullName().trim();
            if (!Objects.equals(existingOrder.getFullName(), newVal)) {
                existingOrder.setFullName(newVal);
                hasChanges = true;
            }
        }

        if (request.getDateOfBirth() != null) {
            if (!Objects.equals(existingOrder.getDateOfBirth(), request.getDateOfBirth())) {
                existingOrder.setDateOfBirth(request.getDateOfBirth());
                existingOrder.setAgeYearsSnapshot(calculateAge(request.getDateOfBirth()));
                hasChanges = true;
            }
        }

        if (request.getGender() != null && !request.getGender().isBlank()) {
            Gender newGender = Gender.valueOf(request.getGender());
            if (!Objects.equals(existingOrder.getGender(), newGender)) {
                existingOrder.setGender(newGender);
                hasChanges = true;
            }
        }

        if (request.getAddress() != null) {
            String newVal = request.getAddress().trim();
            if (!Objects.equals(existingOrder.getAddress(), newVal)) {
                existingOrder.setAddress(newVal);
                hasChanges = true;
            }
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String newVal = request.getPhone().trim();
            if (!Objects.equals(existingOrder.getPhone(), newVal)) {
                existingOrder.setPhone(newVal);
                hasChanges = true;
            }
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newVal = request.getEmail().trim().toLowerCase();
            if (!Objects.equals(existingOrder.getEmail(), newVal)) {
                existingOrder.setEmail(newVal);
                hasChanges = true;
            }
        }

        if (StringUtils.hasText(request.getTestTypeId())) {

            TestType testType = testTypeRepository.findById(request.getTestTypeId())
                    .orElseThrow(() -> new NotFoundException("TestType not found with ID: " + request.getTestTypeId()));

            if (existingOrder.getTestTypeRef() == null ||
                    !Objects.equals(existingOrder.getTestTypeRef().getId(), testType.getId())) {

                existingOrder.setTestTypeRef(testType);
                existingOrder.setTestTypeIdSnapshot(testType.getId());
                existingOrder.setTestTypeNameSnapshot(testType.getName());
                hasChanges = true;
            }
        }

        if (StringUtils.hasText(request.getMedicalRecordCode())) {

            if (!isAutoCreated) {
                throw new BadRequestException("Cannot update medical record for a non-auto-created test order.");
            }

            if (!request.getMedicalRecordCode().equals(existingOrder.getMedicalRecordCode())) {

                ApiResponse<PatientMedicalRecordInternalResponse> patient =
                        patientMedicalRecordFeignClient.getPatientMedicalRecordByCode(request.getMedicalRecordCode());

                PatientMedicalRecordInternalResponse p = patient.getData();

                existingOrder.setMedicalRecordCode(p.medicalRecordCode());
                existingOrder.setMedicalRecordId(p.medicalRecordId());
                existingOrder.setFullName(p.fullName());
                existingOrder.setGender(p.gender());
                existingOrder.setDateOfBirth(p.dateOfBirth().toLocalDate());
                existingOrder.setAgeYearsSnapshot(calculateAge(p.dateOfBirth().toLocalDate()));
                existingOrder.setPhone(p.phone());
                existingOrder.setEmail(p.email());
                existingOrder.setAddress(p.address());

                hasChanges = true;
            }
        }

        if (isAutoCreated && hasChanges) {
            existingOrder.setAutoCreated(false);
            existingOrder.setRequiresPatientMatch(false);

            if (existingOrder.getStatus() == OrderStatus.AUTO_CREATED) {
                existingOrder.setStatus(OrderStatus.COMPLETED);
            }
        }


        if (!hasChanges) {
            log.info("No changes detected for test order: {}", orderCode);
            return testOrderMapper.toResponse(existingOrder, testTypeService.getTestTypeById(existingOrder.getTestTypeRef().getId()));
        }

        // Get current user ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("Cannot create test order without a logged-in user.");
        }

        existingOrder.setUpdatedAt(LocalDateTime.now());
        existingOrder.setUpdatedBy(currentUserId);

        // Save updated order
        TestOrder updatedOrder = testOrderRepository.save(existingOrder);

        eventLogPublisher.publishEvent(SystemEvent.builder()
                .eventCode("E_00002")
                .action("Update Test Order")
                .message("Updated test order " + orderCode)
                .sourceService("TEST_ORDER_SERVICE")
                .operator(SecurityUtils.getCurrentUserId())
                .details(Map.of("orderCode", orderCode, "updates", request)) // Có thể log diff nếu cần
                .build());

        // Log the update event
        orderEventLogService.logOrderUpdate(beforeUpdate, updatedOrder, EventType.UPDATE);

        log.info("Test order {} updated successfully.", orderCode);

        return testOrderMapper.toResponse(updatedOrder, existingOrder.getTestTypeRef() != null ?
                testTypeService.getTestTypeById(existingOrder.getTestTypeRef().getId()) : null);
    }

    // Helper calculate age
    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Math.toIntExact(ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TestOrderResponse> getAllTestOrders(int page, int size, String[] sort, String search,
                                                            LocalDate startDate, LocalDate endDate, OrderStatus status, ReviewStatus reviewStatus,
                                                            ReviewMode reviewMode, Gender gender, String createdBy, String reviewedBy) {
        Sort validSort = SortUtils.buildSort(
                sort,
                SortFields.TEST_ORDER_SORT_FIELDS,
                SortFields.DEFAULT_TEST_ORDER_SORT
        );

        Pageable pageable = PageRequest.of(page, size, validSort);

        Specification<TestOrder> spec = testOrderSpecification.build(search, startDate, endDate, status, reviewStatus, reviewMode, gender, createdBy, reviewedBy);

        Page<TestOrder> testOrderPage = testOrderRepository.findAll(spec, pageable);

        FilterInfo filterInfo = FilterInfo.builder()
                .search(search)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .reviewStatus(reviewStatus)
                .reviewMode(reviewMode)
                .gender(gender)
                .createdBy(createdBy)
                .reviewedBy(reviewedBy)
                .build();

        if (testOrderPage.isEmpty()) {
            return PageResponse.empty(page, size, "No data", filterInfo);
        }

        Page<TestOrderResponse> dtoPage = testOrderPage.map(testOrder -> {

            // 1. Gọi TestTypeService để lấy chi tiết TestType
            TestTypeResponse testTypeResponse = testTypeService.getTestTypeById(testOrder.getTestTypeRef().getId());

            // 2. Map TestOrder → TestOrderResponse (kèm testTypeResponse)
            return testOrderMapper.toResponse(testOrder, testTypeResponse);
        });

        try {
            String details = String.format(
                    "User fetched test orders list [page=%d, size=%d, sort=%s, filters=%s]",
                    page, size, Arrays.toString(sort), new ObjectMapper().writeValueAsString(filterInfo)
            );
            orderEventLogService.logEvent(null, EventType.VIEW_ALL, details);
        } catch (JsonProcessingException e) {
            orderEventLogService.logEvent(null, EventType.VIEW_ALL, "User fetched test orders list (unable to serialize filters)");
        }

        return PageResponse.from(dtoPage, filterInfo);
    }

    @Override
    @Transactional
    public PrintJobResponse requestPrintOrder(String orderId, PrintTestOrderRequest request) {
        if ("SYSTEM_ORDER_ID".equals(orderId)) {
            throw new BadRequestException("Cannot print the system order.");
        }
        // 1. Kiểm tra Test Order
        TestOrder testOrder = testOrderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new NotFoundException("Test order not found with id: " + orderId));

        if (testOrder.isDeleted()) {
            throw new NotFoundException("Test order with id: " + orderId + " has been deleted.");
        }
        if (testOrder.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException("Test order must have status 'Completed' to be printed. Current status: " + testOrder.getStatus());
        }

        // 2. Lấy User ID người yêu cầu
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("Cannot request print job without a logged-in user.");
        }

        // 3. Xử lý các tùy chọn in
        String requestedFileName = null;

        if (request != null) {
            if (request.getCustomFileName() != null && !request.getCustomFileName().isBlank()) {
                requestedFileName = request.getCustomFileName().trim();
            }
        }

        // 3b. Lấy comments
        log.info("Including comments for print job for order: {}", orderId);
        List<String> resultIds = testOrder.getResults()
                .stream()
                .map(TestResult::getResultId)
                .collect(Collectors.toList());
        List<OrderComment> orderComments = orderCommentRepository
                .findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtAsc(
                        CommentTargetType.ORDER,
                        testOrder.getOrderId()
                );
        List<OrderComment> resultComments = new ArrayList<>();
        if (!resultIds.isEmpty()) {
            resultComments = orderCommentRepository
                    .findByTargetTypeAndTargetIdInAndParentIdIsNullOrderByCreatedAtAsc(
                            CommentTargetType.RESULT,
                            resultIds
                    );
        }
        List<CommentOrderResponse> commentsToPrint = Stream.concat(orderComments.stream(), resultComments.stream())
                .sorted(Comparator.comparing(OrderComment::getCreatedAt))
                .map(this::mapToCommentOrderResponse) // Dùng lại helper map thủ công
                .collect(Collectors.toList());

        // 4. Tạo đối tượng ReportJob
        ReportJob printJob = ReportJob.builder()
                .jobType(JobType.PRINT_ORDER_PDF)
                .status(JobStatus.QUEUED)
                .requestedBy(currentUserId)
                .orderId(orderId)
                .printOrderRef(testOrder)
                // Truyền tham số đã loại bỏ customSavePath
                .paramsJson(createParamsJson(requestedFileName, commentsToPrint))
                .build();

        ReportJob savedJob = reportJobRepository.save(printJob);

        // 5. Đưa Job ID vào hàng đợi xử lý ngầm
        pdfGenerationQueueService.queuePdfGeneration(savedJob.getJobId());

        // 6. Ghi log sự kiện
        String logDetails = "Print job requested with Job ID: " + savedJob.getJobId() + " (Comments included)";
        orderEventLogService.logEvent(testOrder, EventType.PRINT_REQUEST, logDetails);

        // 7. Trả về thông tin job cho client
        return PrintJobResponse.builder()
                .jobId(savedJob.getJobId())
                .orderId(orderId)
                .status(savedJob.getStatus())
                .message("Print job successfully queued.")
                .requestedAt(savedJob.getCreatedAt())
                .build();
    }

    // Helper tạo JSON
    private String createParamsJson(String customFileName, List<CommentOrderResponse> comments) {
        Map<String, Object> params = new HashMap<>();

        if (customFileName != null) {
            params.put("customFileName", customFileName);
        }

        // Luôn thêm comments (nếu nó không rỗng)
        if (comments != null && !comments.isEmpty()) {
            params.put("comments", comments);
        }

        if (params.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize print job params to JSON", e);
            return null;
        }
    }

    @Override
    @Transactional
    public PrintJobResponse requestExportExcel(ExportExcelRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("Cannot request export job without a logged-in user.");
        }

        // --- Xử lý logic cho việc xác định phạm vi export ---
        List<String> targetOrderIds = new ArrayList<>();
        String dateRangeType = request.getDateRangeType() != null ? request.getDateRangeType() : "THIS_MONTH"; // Mặc định là THIS_MONTH
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        boolean useDateRange = false; // Cờ xác định có dùng lọc theo ngày không

        if (request.getOrderIds() != null && !request.getOrderIds().isEmpty()) {
            // Ưu tiên export theo danh sách ID nếu có
            targetOrderIds = request.getOrderIds();
            long count = testOrderRepository.countByOrderIdInAndDeletedFalse(targetOrderIds);
            if (count != targetOrderIds.size()) {
                List<String> foundIds = testOrderRepository.findByOrderIdInAndDeletedFalse(targetOrderIds)
                        .stream().map(TestOrder::getOrderId).toList();
                List<String> missingIds = targetOrderIds.stream().filter(id -> !foundIds.contains(id)).toList();
                throw new NotFoundException("One or more Test Orders not found or deleted: " + String.join(", ", missingIds));
            }
            log.info("Exporting specific {} non-deleted orders by ID list.", targetOrderIds.size());
            // Khi export theo ID list, không dùng lọc theo ngày
            dateRangeType = null; // Reset dateRangeType để worker không query theo ngày nữa
            startDate = null;
            endDate = null;
        } else {
            // Nếu không có orderIds, sẽ export theo khoảng thời gian
            useDateRange = true;
            log.info("Exporting orders based on date range type: {}", dateRangeType);
            if ("CUSTOM".equals(dateRangeType)) {
                log.info("Custom date range: {} to {}", startDate, endDate);
                // Validation đã kiểm tra startDate/endDate không null
            }
        }

        // Lấy tên file tùy chỉnh (giữ nguyên)
        String requestedFileName = null;
        if (request.getCustomFileName() != null && !request.getCustomFileName().isBlank()) {
            requestedFileName = request.getCustomFileName().trim();
        }

        // Tạo ReportJob
        ReportJob exportJob = ReportJob.builder()
                .jobType(JobType.EXPORT_ORDERS_XLSX)
                .status(JobStatus.QUEUED)
                .requestedBy(currentUserId)
                // Lưu tham số mới vào JSON
                .paramsJson(createExportParamsJson(targetOrderIds, requestedFileName, dateRangeType, startDate, endDate))
                .build();

        ReportJob savedJob = reportJobRepository.save(exportJob);

        // Đưa vào hàng đợi Excel
        excelGenerationQueueService.queueExcelGeneration(savedJob.getJobId());
        orderEventLogService.logEvent(null, EventType.EXPORT_REQUEST, "Excel export job requested with Job ID: " + savedJob.getJobId());

        // Trả về response
        return PrintJobResponse.builder()
                .jobId(savedJob.getJobId())
                .orderId(null)
                .status(savedJob.getStatus())
                .message("Export Excel job successfully queued.")
                .requestedAt(savedJob.getCreatedAt())
                .build();
    }

    // Helper mới để tạo JSON cho export excel
    private String createExportParamsJson(List<String> orderIds, String customFileName,
                                          String dateRangeType, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> params = new HashMap<>();

        if (orderIds != null && !orderIds.isEmpty()) {
            params.put("orderIds", orderIds);
        }
        if (customFileName != null) {
            params.put("customFileName", customFileName);
        }

        // Thêm các tham số thời gian
        if (dateRangeType != null) { // Chỉ thêm nếu không phải export theo ID list
            params.put("dateRangeType", dateRangeType);
            if ("CUSTOM".equals(dateRangeType)) {
                // Chuyển LocalDate thành String YYYY-MM-DD để lưu vào JSON
                params.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                params.put("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }

        // Không cần kiểm tra empty vì nếu không có ID thì dateRangeType sẽ có giá trị
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("Error creating params JSON for excel export job", e);
            return null;
        }
    }

    @Override
    @Transactional
    public ReviewTestOrderResponse reviewTestOrder(String orderId, ReviewTestOrderHl7Request request) {
        String currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Validate Acceptance Criteria: Tồn tại, chưa xóa
        TestOrder testOrder = testOrderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new NotFoundException("Test order not found or has been deleted: " + orderId));

        // 2. Validate Acceptance Criteria: Phải ở trạng thái COMPLETED
        if (testOrder.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException(
                    "Test order must be in 'COMPLETED' status to be reviewed. Current status: " + testOrder.getStatus()
            );
        }

        ReviewMode mode = request.getReviewMode();
        String hl7Status = "NOT_ATTEMPTED";
        int adjustmentsCount = 0;
        List<TestResultAdjustLog> logsToSave = new ArrayList<>();

        // 3. Xử lý điều chỉnh bằng HL7 (nếu có)
        if (request.getHl7Message() != null && !request.getHl7Message().isBlank()) {
            log.info("Processing HL7 adjustments for review of order: {}", orderId);

            // 3a. Parse tin nhắn HL7 để "đọc trước"
            List<ParsedTestResult> parsedResults;
            try {
                parsedResults = hl7ParserService.parseHl7Message(request.getHl7Message());
            } catch (Exception e) {
                log.error("Failed to parse HL7 message during review validation", e);
                throw new BadRequestException("HL7 message is malformed and cannot be parsed: " + e.getMessage());
            }

            if (parsedResults == null || parsedResults.isEmpty()) {
                throw new BadRequestException("HL7 adjustment message does not contain any valid test results (OBX segments).");
            }

            // 3b. Validation: Order ID trong payload phải khớp với Order ID đang review (từ URL)
            String orderIdFromPayload = parsedResults.get(0).getOrderId();
            if (orderIdFromPayload == null || !Objects.equals(orderId, orderIdFromPayload)) {
                throw new BadRequestException(
                        String.format("HL7 message mismatch: The message is for Order ID '%s', but you are reviewing Order ID '%s'.",
                                orderIdFromPayload, orderId)
                );
            }

            // 3c. *** LOGIC ĐIỀU CHỈNH MỚI ***
            // Chúng ta không gọi Hl7ProcessingService, mà tự xử lý việc cập nhật
            for (ParsedTestResult parsed : parsedResults) {
                String analyteName = parsed.getAnalyteName();
                String newValue = parsed.getValueText();

                if (analyteName == null || newValue == null) {
                    log.warn("Skipping adjustment for order {}: AnalyteName or ValueText is null in HL7 OBX segment.", orderId);
                    continue;
                }

                // Tìm TestResult HIỆN TẠI bằng OrderId và AnalyteName
                List<TestResult> existingResults = testResultRepository.findByOrderIdAndAnalyteNameIgnoreCase(orderId, analyteName);

                if (existingResults.isEmpty()) {
                    log.warn("Adjustment skipped: Cannot find an existing TestResult for Order {} and Analyte '{}'", orderId, analyteName);
                    continue;
                }

                if (existingResults.size() > 1) {
                    log.warn("Adjustment skipped: Found {} duplicate TestResults for Order {} and Analyte '{}'. Manual correction required.",
                            existingResults.size(), orderId, analyteName);
                    continue;
                }

                TestResult resultToUpdate = existingResults.get(0);
                String beforeValue = resultToUpdate.getValueText();

                // Chỉ cập nhật và ghi log nếu giá trị thực sự thay đổi
                if (Objects.equals(beforeValue, newValue)) {
                    log.info("Adjustment skipped for Analyte '{}': Value is already correct.", analyteName);
                    continue;
                }

                // Cập nhật các trường liên quan từ HL7
                resultToUpdate.setValueText(newValue);
                resultToUpdate.setAbnormalFlag(parsed.getAbnormalFlag()); // Cập nhật cờ
                resultToUpdate.setReferenceRange(parsed.getReferenceRange()); // Cập nhật dải tham chiếu
                resultToUpdate.setUnit(parsed.getUnit()); // Cập nhật đơn vị
                resultToUpdate.setMeasuredAt(parsed.getMeasuredAt()); // Cập nhật thời gian đo
                resultToUpdate.setTestCode(parsed.getTestCode());
                // Đánh dấu là đã được điều chỉnh (nếu bạn có trường này)
                // resultToUpdate.setEdited(true);

                testResultRepository.save(resultToUpdate);

                // Tạo TestResultAdjustLog
                TestResultAdjustLog logEntry = TestResultAdjustLog.builder()
                        .orderId(orderId)
                        .resultId(resultToUpdate.getResultId())
                        .reviewMode(mode)
                        .actorUserId(currentUserId)
                        .field("valueText") // Trường chính được thay đổi
                        .beforeValue(beforeValue)
                        .afterValue(newValue)
                        .note("Adjusted via HL7 review. Note: " + request.getNote())
                        .build();

                logsToSave.add(logEntry);
                adjustmentsCount++;

                eventLogPublisher.publishEvent(SystemEvent.builder()
                        .eventCode("E_00004")
                        .action("Modify Test Result")
                        .message("Modified results for order " + orderId)
                        .sourceService("TEST_ORDER_SERVICE")
                        .operator(SecurityUtils.getCurrentUserId())
                        .details(Map.of("orderId", orderId, "updates", request))
                        .build());
            }

            if (!logsToSave.isEmpty()) {
                testResultAdjustLogRepository.saveAll(logsToSave);
            }

            hl7Status = "PROCESSED_SUCCESSFULLY";

        } else {
            log.info("Review for order {} submitted with no HL7 adjustments.", orderId);
            hl7Status = "NOT_PROVIDED";
        }

        // 4. Cập nhật trạng thái Review của TestOrder
        testOrder.setReviewStatus(ReviewStatus.HUMAN_REVIEWED);
        testOrder.setReviewMode(mode);
        testOrder.setReviewedBy(currentUserId);
        testOrder.setReviewedAt(LocalDateTime.now(ZoneOffset.UTC));
        testOrderRepository.save(testOrder);

        // 5. Ghi log sự kiện Review chính
        EventType eventType = (mode == ReviewMode.AI) ? EventType.REVIEW_AI : EventType.REVIEW_HUMAN;
        String logDetails = String.format("Order reviewed. Mode: %s. HL7 Adjustments: %s (%d results). Note: %s",
                mode, hl7Status, adjustmentsCount, request.getNote());

        orderEventLogService.logEvent(testOrder, eventType, logDetails);

        eventLogPublisher.publishEvent(SystemEvent.builder()
                .eventCode("E_00008")
                .action("Review Test Order Results")
                .message("Completed review for order " + orderId)
                .sourceService("TEST_ORDER_SERVICE")
                .operator(SecurityUtils.getCurrentUserId())
                .details(Map.of("orderId", orderId, "status", "HUMAN_REVIEWED"))
                .build());

        // 6. Trả về Response
        return ReviewTestOrderResponse.builder()
                .orderId(orderId)
                .reviewStatus(testOrder.getReviewStatus())
                .reviewedBy(currentUserId)
                .reviewedAt(testOrder.getReviewedAt())
                .adjustmentsLogged(adjustmentsCount)
                .message("Test order reviewed successfully. HL7 adjustments: " + hl7Status)
                .build();
    }

    @Override
    public TestOrderResponse createShellOrderFromBarcode(String barcode) {
        log.info("Auto-creating shell test order for barcode: {}", barcode);

        // TODO: Cân nhắc kiểm tra barcode trùng lặp nếu nghiệp vụ yêu cầu
        // Ví dụ: if (testOrderRepository.existsByBarcode(barcode)) { ... }

        TestOrder testOrder = new TestOrder();
        testOrder.setBarcode(barcode); // (Bạn sẽ cần thêm trường này ở bước 4)
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setEntrySource(EntrySource.AUTO_INSTRUMENT); // (Bạn sẽ cần thêm ở bước 5)
        testOrder.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        // Đặt createdBy là một giá trị hệ thống đặc biệt
        // vì không có người dùng nào đăng nhập khi máy gọi
        testOrder.setCreatedBy("SYSTEM_AUTO_CREATE");

        TestTypeResponse testTypeResponse = testTypeService.getTestTypeById(testOrder.getTestTypeRef().getId());

        TestOrder savedTestOrder = testOrderRepository.save(testOrder);

        // Ghi log sự kiện
        orderEventLogService.logEvent(savedTestOrder, EventType.CREATE,
                "Shell test order auto-created from instrument for barcode: " + barcode);

        return testOrderMapper.toResponse(savedTestOrder, testTypeResponse);
    }

    @Override
    @Transactional
    public TestOrderResponse autoCreateTestOrder(AutoCreateTestOrderRequest request) {
        log.info("[AUTO-CREATE] Creating test order for barcode: {}", request.getBarcode());

        if (!StringUtils.hasText(request.getBarcode())) {
            throw new BadRequestException("Barcode is required for auto-create.");
        }

        // Kiểm tra nếu đã tồn tại đơn với barcode này chưa
        Optional<TestOrder> existing = testOrderRepository.findByBarcode(request.getBarcode());
        if (existing.isPresent()) {
            log.warn("[AUTO-CREATE] Test order already exists for barcode {}. Returning existing.", request.getBarcode());
            // Chưa có TestTypeSnapshot nên truyền null
            return testOrderMapper.toResponse(existing.get(), null);
        }

        // Tạo entity mới
        TestOrder testOrder = new TestOrder();

        // Tạo id và code cho đơn
        testOrder.setOrderId(TestOrderGenerator.generateTestOrderId());

        testOrder.setOrderCode(TestOrderGenerator.generateTestOrderCode());

        // Barcode này là barcode từ ống mẫu (sample)
        testOrder.setBarcode(request.getBarcode());
        testOrder.setReviewMode(ReviewMode.HUMAN);
        testOrder.setReviewStatus(ReviewStatus.NONE);

        // Trạng thái đặc biệt cho đơn auto-created
        testOrder.setStatus(OrderStatus.AUTO_CREATED);
        testOrder.setAutoCreated(true);
        testOrder.setRequiresPatientMatch(true);

        testOrder.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        // Vì là tạo từ Instrument, không có user login
        testOrder.setCreatedBy("INSTRUMENT_SERVICE");

        // Không set:
        // - testType
        // - medicalRecordCode
        // - patient info
        // Các thông tin này sẽ được người dùng cập nhật sau trên LIS.

        TestOrder saved = testOrderRepository.save(testOrder);

        // Log event
        orderEventLogService.logEvent(
                saved,
                EventType.CREATE,
                "Auto-created from Instrument for barcode: " + saved.getBarcode()
        );

        // Trả về response. Không có TestType kèm theo nên truyền null
        return testOrderMapper.toResponse(saved, null);
    }
}