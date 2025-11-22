package fit.test_order_service.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import fit.test_order_service.client.WarehouseFeignClient;
import fit.test_order_service.dtos.response.TestParameterResponse;
import fit.test_order_service.dtos.request.CreateTestTypeRequest;
import fit.test_order_service.dtos.request.UpdateTestTypeRequest;
import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.dtos.response.FilterInfo;
import fit.test_order_service.dtos.response.PageResponse;
import fit.test_order_service.dtos.response.TestTypeResponse;
import fit.test_order_service.entities.TestType;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.repositories.TestTypeRepository;
import fit.test_order_service.services.TestTypeService;
import fit.test_order_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestTypeServiceImpl implements TestTypeService {

    private final TestTypeRepository testTypeRepository;
    private final WarehouseFeignClient warehouseFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TestTypeResponse createTestType(CreateTestTypeRequest request) {
        // 1. Validate trùng tên
        if (testTypeRepository.existsByName(request.getName())) {
            throw new BadRequestException("TestType with name '" + request.getName() + "' already exists.");
        }

        // 2. Validate Test Parameters từ Warehouse
        // (Nếu user có gửi list params)
        if (request.getTestParameterIds() != null && !request.getTestParameterIds().isEmpty()) {
            try {
                log.info("Validating test parameters with Warehouse: {}", request.getTestParameterIds());
                ApiResponse<Boolean> validResponse = warehouseFeignClient.validateTestParameters(request.getTestParameterIds());

                if (validResponse == null || !Boolean.TRUE.equals(validResponse.getData())) {
                    throw new BadRequestException("One or more TestParameter IDs are invalid or not found in Warehouse.");
                }
            } catch (Exception e) {
                log.error("Error calling Warehouse Service", e);
                throw new BadRequestException("Failed to validate test parameters with Warehouse Service: " + e.getMessage());
            }
        }

        try {
            ApiResponse<Boolean> reagentAvailable =
                    warehouseFeignClient.checkReagentAvailability(
                            request.getReagentName(),
                            request.getRequiredVolume()
                    );

            if (reagentAvailable == null || !Boolean.TRUE.equals(reagentAvailable.getData())) {
                throw new BadRequestException("Reagent '" + request.getReagentName() +
                        "' does not have enough volume in Warehouse.");
            }

        } catch (FeignException e) {

            String raw = e.contentUTF8();
            log.error("Warehouse raw body = {}", raw);

            try {
                JsonNode node = objectMapper.readTree(raw);
                log.error("Parsed Warehouse error response: {}", node);

                if (node.has("message")) {
                    // Trả message
                    throw new BadRequestException(node.get("message").asText());
                }

                throw new BadRequestException("Warehouse Service error");

            } catch (BadRequestException bre) {
                // Bắt lại BadRequestException -> Ném ra luôn
                throw bre;

            } catch (Exception parseEx) {
                throw new BadRequestException("Warehouse Service unreachable");
            }
        }

        // 3. Convert List IDs -> JSON String
        String paramsJson = "[]";
        if (request.getTestParameterIds() != null) {
            try {
                paramsJson = objectMapper.writeValueAsString(request.getTestParameterIds());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing parameters JSON", e);
            }
        }

        // 4. Tạo và Lưu Entity
        TestType testType = TestType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .testParametersJson(paramsJson)
                .reagentName(request.getReagentName())
                .requiredVolume(request.getRequiredVolume())
                .createdBy(SecurityUtils.getCurrentUserId())
                .build();


        TestType savedType = testTypeRepository.save(testType);

        // Parse test parameter IDs từ JSON
        List<String> paramIds = new ArrayList<>();
        try {
            if (savedType.getTestParametersJson() != null) {
                paramIds = objectMapper.readValue(
                        savedType.getTestParametersJson(),
                        new TypeReference<List<String>>() {
                        }
                );
            }
        } catch (Exception e) {
            log.error("Error parsing testParametersJson", e);
        }

        // === GỌI WAREHOUSE SERVICE ĐỂ LẤY DANH SÁCH PARAM ===
        List<TestParameterResponse> paramResponses = new ArrayList<>();

        for (String paramId : paramIds) {
            try {
                ApiResponse<TestParameterResponse> apiRes =
                        warehouseFeignClient.getTestParameterByTestParameterId(paramId);

                if (apiRes != null && apiRes.getData() != null) {
                    paramResponses.add(apiRes.getData());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch TestParameter {} from Warehouse", paramId);
            }
        }

        // 5. Map sang Response
        return toResponse(savedType, paramResponses);
    }

    private String extractJson(String body) {
        int start = body.indexOf('{');
        int end = body.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return body.substring(start, end + 1);
        }
        return null;
    }

    @Override
    public TestTypeResponse getTestTypeById(String testTypeId) {
        TestType testType = testTypeRepository.findById(testTypeId)
                .orElseThrow(() -> new BadRequestException("TestType not found with ID: " + testTypeId));

        // Parse test parameter IDs từ JSON
        List<String> paramIds = new ArrayList<>();
        try {
            if (testType.getTestParametersJson() != null) {
                paramIds = objectMapper.readValue(
                        testType.getTestParametersJson(),
                        new TypeReference<List<String>>() {
                        }
                );
            }
        } catch (Exception e) {
            log.error("Error parsing testParametersJson", e);
        }

        // === GỌI WAREHOUSE SERVICE ĐỂ LẤY DANH SÁCH PARAM ===
        List<TestParameterResponse> paramResponses = new ArrayList<>();

        for (String paramId : paramIds) {
            try {
                ApiResponse<TestParameterResponse> apiRes =
                        warehouseFeignClient.getTestParameterByTestParameterId(paramId);

                if (apiRes != null && apiRes.getData() != null) {
                    paramResponses.add(apiRes.getData());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch TestParameter {} from Warehouse", paramId);
            }
        }

        // Map sang Response
        return toResponse(testType, paramResponses);
    }


    // Helper method để map entity sang response
    private TestTypeResponse toResponse(TestType entity, List<TestParameterResponse> testParameters) {
        return TestTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .testParametersJson(entity.getTestParametersJson())
                .reagentName(entity.getReagentName())
                .requiredVolume(entity.getRequiredVolume())
                .createdAt(entity.getCreatedAt())
                .createdBy(SecurityUtils.getCurrentUserId())
                .testParameters(testParameters)
                .build();
    }

    @Override
    public PageResponse<TestTypeResponse> getAllTestTypes(int page, int size, String[] sort, String search) {
        // 1. Xử lý Sorting (Sử dụng SortUtils nếu có, hoặc tự build)
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null) {
            // Giả sử format sort là ["field,direction", "field2,asc"]
            // Logic này tùy thuộc vào cách bạn quy định param sort, ví dụ đơn giản:
            try {
                for (String sortOrder : sort) {
                    if (sortOrder.contains(",")) {
                        String[] _sort = sortOrder.split(",");
                        orders.add(new Sort.Order(Sort.Direction.fromString(_sort[1]), _sort[0]));
                    } else {
                        orders.add(new Sort.Order(Sort.Direction.ASC, sortOrder));
                    }
                }
            } catch (Exception e) {
                // Fallback hoặc log warning
                orders.add(new Sort.Order(Sort.Direction.DESC, "createdAt"));
            }
        } else {
            orders.add(new Sort.Order(Sort.Direction.DESC, "createdAt"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        // 2. Query Database lấy Entities (đã phân trang)
        Page<TestType> testTypePage = testTypeRepository.searchTestTypes(search, pageable);

        // 3. Map và Enrich Data (Gọi Warehouse cho từng item trong trang này)
        List<TestTypeResponse> responseList = testTypePage.getContent().stream()
                .map(this::enrichAndMapToResponse)
                .toList();

        // 4. Tạo PageImpl mới chứa DTO response
        Page<TestTypeResponse> pageResponse = new PageImpl<>(
                responseList,
                pageable,
                testTypePage.getTotalElements()
        );

        // 5. Trả về PageResponse chuẩn
        // Tạo FilterInfo (nếu cần trả về metadata của bộ lọc)
        FilterInfo filterInfo = FilterInfo.builder()
                // .keyword(search) // Nếu FilterInfo của bạn có field này
                .build();

        return PageResponse.from(pageResponse, filterInfo);
    }

    @Override
    @Transactional
    public TestTypeResponse updateTestType(String id, UpdateTestTypeRequest request) {
        // 1. Tìm TestType cũ
        TestType existingTestType = testTypeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("TestType not found with ID: " + id));

        // 2. Cập nhật NAME
        // Sử dụng trim() để loại bỏ khoảng trắng thừa đầu/cuối tránh lỗi logic
        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = request.getName().trim();

            // Chỉ check trùng nếu tên thực sự thay đổi
            if (!existingTestType.getName().equals(newName)) {
                // Kiểm tra xem có TestType nào KHÁC (khác ID hiện tại) đang dùng tên này không
                if (testTypeRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
                    throw new BadRequestException("TestType with name '" + newName + "' already exists.");
                }
                existingTestType.setName(newName);
            }
        }

        // 3. Cập nhật DESCRIPTION
        if (request.getDescription() != null) {
            existingTestType.setDescription(request.getDescription());
        }

        // 4. Xử lý REAGENT & VOLUME
        // Logic: Nếu có gửi 1 trong 2 trường này lên thì phải validate lại với Warehouse
        boolean isReagentChanged = (request.getReagentName() != null && !request.getReagentName().isBlank())
                || (request.getRequiredVolume() != null);

        if (isReagentChanged) {
            // Lấy giá trị mới (ưu tiên) hoặc giữ giá trị cũ
            String targetReagentName = (request.getReagentName() != null && !request.getReagentName().isBlank())
                    ? request.getReagentName().trim()
                    : existingTestType.getReagentName();

            Double targetVolume = (request.getRequiredVolume() != null)
                    ? request.getRequiredVolume()
                    : existingTestType.getRequiredVolume();

            // Gọi Warehouse để kiểm tra
            try {
                ApiResponse<Boolean> reagentAvailable = warehouseFeignClient.checkReagentAvailability(
                        targetReagentName,
                        targetVolume
                );
                // ... (giữ nguyên logic check null)
            } catch (FeignException e) {
                log.error("Feign Error: Status={}, Body={}", e.status(), e.contentUTF8());

                String errorMsg = "Warehouse Service error";
                try {
                    String raw = e.contentUTF8();
                    JsonNode node = objectMapper.readTree(raw);

                    // Case 1: Nếu response là Array (như log bạn gửi: [{"status":400...}])
                    if (node.isArray() && node.size() > 0) {
                        JsonNode firstNode = node.get(0);
                        if (firstNode.has("message")) {
                            errorMsg = firstNode.get("message").asText();
                        }
                    }
                    // Case 2: Nếu response là Object thông thường
                    else if (node.has("message")) {
                        errorMsg = node.get("message").asText();
                    }

                } catch (Exception ignored) {
                    // Fallback nếu không parse được JSON
                    if (e.status() == 404) {
                        errorMsg = "Reagent '" + targetReagentName + "' not found in Warehouse.";
                    }
                }

                // Ném lỗi 400 với message sạch sẽ
                throw new BadRequestException(errorMsg);
            }

            // Nếu OK thì mới set vào entity
            if (request.getReagentName() != null && !request.getReagentName().isBlank()) {
                existingTestType.setReagentName(request.getReagentName().trim());
            }
            if (request.getRequiredVolume() != null) {
                existingTestType.setRequiredVolume(request.getRequiredVolume());
            }
        }

        // 5. CẬP NHẬT AUDIT INFO (QUAN TRỌNG: Đặt ở đây để luôn chạy)
        existingTestType.setUpdatedAt(LocalDateTime.now());

        try {
            String currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId != null) {
                existingTestType.setUpdatedBy(currentUserId);
            }
        } catch (Exception e) {
            log.warn("Could not get current user id for update audit: {}", e.getMessage());
        }

        // 6. Save & Return
        TestType updatedType = testTypeRepository.save(existingTestType);
        return enrichAndMapToResponse(updatedType);
    }

    // Hàm helper: Thực hiện lấy dữ liệu từ Warehouse và map sang Response
    private TestTypeResponse enrichAndMapToResponse(TestType testType) {
        List<TestParameterResponse> paramResponses = fetchWarehouseParameters(testType.getTestParametersJson());
        return toResponse(testType, paramResponses);
    }

    // Hàm helper: Parse JSON và gọi Feign Client (Tách logic chung ra đây)
    private List<TestParameterResponse> fetchWarehouseParameters(String jsonParams) {
        if (jsonParams == null || jsonParams.isBlank()) {
            return Collections.emptyList();
        }

        List<String> paramIds = new ArrayList<>();
        try {
            paramIds = objectMapper.readValue(jsonParams, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Error parsing testParametersJson: {}", jsonParams, e);
            return Collections.emptyList();
        }

        List<TestParameterResponse> paramResponses = new ArrayList<>();
        for (String paramId : paramIds) {
            try {
                ApiResponse<TestParameterResponse> apiRes = warehouseFeignClient.getTestParameterByTestParameterId(paramId);
                if (apiRes != null && apiRes.getData() != null) {
                    paramResponses.add(apiRes.getData());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch TestParameter {} from Warehouse", paramId);
            }
        }
        return paramResponses;
    }
}