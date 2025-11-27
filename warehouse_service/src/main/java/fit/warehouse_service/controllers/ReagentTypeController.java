/*
 * @ (#) ReagentTypeController.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.controllers;

import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.ReagentTypeResponse;
import fit.warehouse_service.services.ReagentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for managing ReagentType entities.
 */
@RestController
@RequestMapping("/api/v1/warehouse/reagent-types")
@RequiredArgsConstructor
public class ReagentTypeController {

    private final ReagentTypeService reagentTypeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','LAB_USER', 'DOCTOR', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<ReagentTypeResponse>>> getAllReagentTypes() {

        // Gọi service để lấy danh sách ReagentType
        List<ReagentTypeResponse> responseDtos = reagentTypeService.getAllReagentType();

        // Xử lý trường hợp không tìm thấy dữ liệu (trả về message "No Data")
        if (responseDtos.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.<List<ReagentTypeResponse>>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("No Reagent Types found.")
                    .timestamp(LocalDateTime.now())
                    .data(null)
                    .build());
        }

        // Trả về response thành công
        ApiResponse<List<ReagentTypeResponse>> apiResponse = ApiResponse.<List<ReagentTypeResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Reagent Types retrieved successfully.")
                .timestamp(LocalDateTime.now())
                .data(responseDtos)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}