/*
 * @ {#} TestParameterController.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.controllers;

import fit.warehouse_service.dtos.request.CreateTestParameterRequest;
import fit.warehouse_service.dtos.request.UpdateTestParameterRequest;
import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.dtos.response.TestParameterResponse;
import fit.warehouse_service.services.TestParameterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/*
 * @description: Controller for managing Test Parameters
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/warehouse/test-parameters")
@RequiredArgsConstructor
public class TestParameterController {
    private final TestParameterService testParameterService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<TestParameterResponse>> createTestParameter(
            @Valid @RequestBody CreateTestParameterRequest request) {
        TestParameterResponse response = testParameterService.createTestParameter(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Test parameter created successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/abbreviation/{abbreviation}")
    public ResponseEntity<ApiResponse<TestParameterResponse>> getTestParameterByAbbreviation(
            @PathVariable String abbreviation) {
        TestParameterResponse response = testParameterService.getTestParameterByAbbreviation(abbreviation);
        return ResponseEntity.ok(ApiResponse.success(response, "Test parameter retrieved successfully"));}

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{testParameterId}")
    public ResponseEntity<ApiResponse<TestParameterResponse>> getTestParameterByTestParameterId(
            @PathVariable String testParameterId) {
        TestParameterResponse response = testParameterService.getTestParameterByTestParameterId(testParameterId);
        return ResponseEntity.ok(ApiResponse.success(response, "Test parameter retrieved successfully"));}

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{testParameterId}")
    public ResponseEntity<ApiResponse<TestParameterResponse>> updateTestParameter(
            @PathVariable String testParameterId,
            @Valid @RequestBody UpdateTestParameterRequest request) {
        TestParameterResponse response = testParameterService.updateTestParameter(testParameterId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Test parameter updated successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{testParameterId}")
    public ResponseEntity<ApiResponse<Void>> deleteTestParameter(@PathVariable String testParameterId) {
        testParameterService.deleteTestParameter(testParameterId);
        return ResponseEntity.ok(ApiResponse.success(null, "Test parameter deleted successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PatchMapping("/{testParameterId}/restore")
    public ResponseEntity<ApiResponse<TestParameterResponse>> restoreTestParameter(
            @PathVariable String testParameterId) {
        TestParameterResponse response = testParameterService.restoreTestParameter(testParameterId);
        return ResponseEntity.ok(ApiResponse.success(response, "Test parameter restored successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TestParameterResponse>>> getAllTestParameters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PageResponse<TestParameterResponse> response = testParameterService.getAllTestParameters(
                page, size, sort, search, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(response, "Test parameters retrieved successfully"));
    }
    @PostMapping("/validate-ids")
    public ResponseEntity<ApiResponse<Boolean>> validateTestParameters(@RequestBody List<String> ids) {
        boolean isValid = testParameterService.validateTestParametersExist(ids);
        return ResponseEntity.ok(ApiResponse.success(isValid, "Validation result"));
    }
}
