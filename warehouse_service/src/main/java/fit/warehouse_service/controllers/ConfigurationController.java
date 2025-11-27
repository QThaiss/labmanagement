/*
 * @ (#) ConfigurationController.java    1.0    03/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.controllers;/*
 * @description:
 * @author: Bao Thong
 * @date: 03/11/2025
 * @version: 1.0
 */

import fit.warehouse_service.dtos.request.CreateConfigurationRequest;
import fit.warehouse_service.dtos.request.ModifyConfigurationRequest;
import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.ConfigurationResponse;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.enums.DataType;
import fit.warehouse_service.services.ConfigurationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/warehouse/configurations")
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService configurationService;

    /**
     * API để tạo mới một Configuration Setting.
     * Tuân thủ 3.3.3.1 Create Configurations.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ConfigurationResponse>> createConfiguration(
            @Valid @RequestBody CreateConfigurationRequest request) {

        // Yêu cầu 3.3.3.1: "system will process the request"
        ConfigurationResponse responseDto = configurationService.createConfiguration(request);

        // Yêu cầu 3.3.3.1: "confirming that the configuration has been created successfully"
        ApiResponse<ConfigurationResponse> apiResponse = ApiResponse.<ConfigurationResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Configuration created successfully.")
                .timestamp(LocalDateTime.now())
                .data(responseDto)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ConfigurationResponse>> modifyConfiguration(
            @PathVariable String id,
            @Valid @RequestBody ModifyConfigurationRequest request) {

        ConfigurationResponse responseDto = configurationService.modifyConfiguration(id, request);

        // Return success response
        ApiResponse<ConfigurationResponse> apiResponse = ApiResponse.success(responseDto, "Configuration updated successfully.");

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ConfigurationResponse>> getConfigurationById(
            @PathVariable("id") String id) {

        ConfigurationResponse responseDto = configurationService.getConfigurationById(id);

        ApiResponse<ConfigurationResponse> apiResponse = ApiResponse.success(responseDto, "Fetched configuration successfully.");

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<ConfigurationResponse>>> getAllConfigurations(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must not be less than zero")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must not be less than one")
            int size,

            @RequestParam(name = "sort", required = false)
            String[] sort,

            @RequestParam(name = "search", required = false)
            String search,

            // Cập nhật: Thay đổi từ DataType (Enum) sang String configType
            @RequestParam(name = "configType", required = false)
            String configType,

            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        // Truyền configType dạng String vào service
        PageResponse<ConfigurationResponse> response = configurationService.getAllConfigurations(
                page, size, sort, search, configType, startDate, endDate
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Fetched configurations successfully."));
    }

    /**
     * Xóa một cấu hình dựa trên ID.
     * Chỉ người dùng có quyền ADMIN mới có thể thực hiện.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteConfiguration(@PathVariable String id) {
        configurationService.deleteConfiguration(id);

        // Trả về thông báo thành công
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Configuration deleted successfully")
                .timestamp(LocalDateTime.now())
                .status(200)
                .build());
    }
}
