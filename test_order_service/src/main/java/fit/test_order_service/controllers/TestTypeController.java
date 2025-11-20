package fit.test_order_service.controllers;

import fit.test_order_service.dtos.request.CreateTestTypeRequest;
import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.dtos.response.TestTypeResponse;
import fit.test_order_service.services.TestTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-types")
@RequiredArgsConstructor
public class TestTypeController {

    private final TestTypeService testTypeService;

    @PostMapping
    public ResponseEntity<ApiResponse<TestTypeResponse>> createTestType(
            @Valid @RequestBody CreateTestTypeRequest request) {

        TestTypeResponse response = testTypeService.createTestType(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "TestType created successfully."));
    }
}