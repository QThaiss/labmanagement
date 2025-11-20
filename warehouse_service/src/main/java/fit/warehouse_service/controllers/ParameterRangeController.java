/*
 * @ {#} ParameterRangeController.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.controllers;

import fit.warehouse_service.dtos.request.CreateParameterRangeRequest;
import fit.warehouse_service.dtos.request.UpdateParameterRangeRequest;
import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.ParameterRangeResponse;
import fit.warehouse_service.services.ParameterRangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/*
 * @description: REST controller for managing ParameterRange entities
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/warehouse/parameter-ranges")
@RequiredArgsConstructor
public class ParameterRangeController {
    private final ParameterRangeService parameterRangeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ParameterRangeResponse>> createParameterRange(
            @Valid @RequestBody CreateParameterRangeRequest request) {
        ParameterRangeResponse response = parameterRangeService.createParameterRange(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Parameter range created successfully."));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{parameterRangeId}")
    public ResponseEntity<ApiResponse<ParameterRangeResponse>> updateParameterRange(
            @PathVariable String parameterRangeId,
            @Valid @RequestBody UpdateParameterRangeRequest request) {
        ParameterRangeResponse response = parameterRangeService.updateParameterRange(parameterRangeId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Parameter range updated successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{parameterRangeId}")
    public ResponseEntity<ApiResponse<Void>> deleteParameterRange(@PathVariable String parameterRangeId) {
        parameterRangeService.deleteParameterRange(parameterRangeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Parameter range deleted successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PatchMapping("/{parameterRangeId}/restore")
    public ResponseEntity<ApiResponse<ParameterRangeResponse>> restoreParameterRange(
            @PathVariable String parameterRangeId) {
        ParameterRangeResponse response = parameterRangeService.restoreParameterRange(parameterRangeId);
        return ResponseEntity.ok(ApiResponse.success(response, "Parameter range restored successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{parameterRangeId}")
    public ResponseEntity<ApiResponse<ParameterRangeResponse>> getParameterRangeById(
            @PathVariable String parameterRangeId) {
        ParameterRangeResponse response = parameterRangeService.getParameterRangeById(parameterRangeId);
        return ResponseEntity.ok(ApiResponse.success(response, "Parameter range retrieved successfully"));
    }
}
