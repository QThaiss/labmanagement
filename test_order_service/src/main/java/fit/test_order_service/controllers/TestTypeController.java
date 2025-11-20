package fit.test_order_service.controllers;

import fit.test_order_service.dtos.request.CreateTestTypeRequest;
import fit.test_order_service.dtos.request.UpdateTestTypeRequest;
import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.dtos.response.PageResponse;
import fit.test_order_service.dtos.response.TestTypeResponse;
import fit.test_order_service.services.TestTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TestTypeResponse>>> getAllTestTypes(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must not be less than zero")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must not be less than one")
            int size,

            @RequestParam(required = false) String[] sort,
            @RequestParam(required = false) String search
    ) {
        PageResponse<TestTypeResponse> response = testTypeService.getAllTestTypes(page, size, sort, search);
        return ResponseEntity.ok(ApiResponse.success(response, "Test types retrieved successfully."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TestTypeResponse>> updateTestType(
            @PathVariable String id,
            @Valid @RequestBody UpdateTestTypeRequest request) {

        TestTypeResponse response = testTypeService.updateTestType(id, request);

        return ResponseEntity.ok(ApiResponse.success(response, "TestType updated successfully."));
    }
}