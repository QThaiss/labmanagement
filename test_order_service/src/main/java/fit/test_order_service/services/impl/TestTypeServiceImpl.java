package fit.test_order_service.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.test_order_service.client.WarehouseFeignClient;
import fit.test_order_service.dtos.request.CreateTestTypeRequest;
import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.dtos.response.TestTypeResponse;
import fit.test_order_service.entities.TestType;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.repositories.TestTypeRepository;
import fit.test_order_service.services.TestTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

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
                .requiredVolume(request.getRequiredVolume()) // Giờ đã là double, không bị lỗi
                .build();

        TestType savedType = testTypeRepository.save(testType);

        // 5. Map sang Response
        return toResponse(savedType);
    }

    // Helper method để map entity sang response
    private TestTypeResponse toResponse(TestType entity) {
        return TestTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .testParametersJson(entity.getTestParametersJson())
                .reagentName(entity.getReagentName())
                .requiredVolume(entity.getRequiredVolume())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}