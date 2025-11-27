/*
 * @ (#) InstrumentController.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.controllers;

/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.dtos.request.ActivateInstrumentRequest;
import fit.warehouse_service.dtos.request.CheckInstrumentStatusRequest;
import fit.warehouse_service.dtos.request.CreateInstrumentRequest;
import fit.warehouse_service.dtos.request.DeactivateInstrumentRequest;
import fit.warehouse_service.dtos.response.*;
import fit.warehouse_service.entities.Instrument;
import fit.warehouse_service.mappers.InstrumentMapper;
import fit.warehouse_service.services.InstrumentService;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/warehouse/instruments")
@RequiredArgsConstructor
public class InstrumentController {
    private final InstrumentService instrumentService;
    private final InstrumentMapper instrumentMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<InstrumentResponse>> addInstrument(
            @Valid @RequestBody CreateInstrumentRequest request) {

        Instrument newInstrument = instrumentService.createInstrument(request);

        InstrumentResponse responseDto = instrumentMapper.toResponse(newInstrument);

        ApiResponse<InstrumentResponse> apiResponse = ApiResponse.<InstrumentResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Instrument created successfully.")
                .timestamp(LocalDateTime.now())
                .data(responseDto)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/status/check")
    public ResponseEntity<ApiResponse<InstrumentStatusResponse>> checkInstrumentStatus(
            @Valid @RequestBody CheckInstrumentStatusRequest request) {
        InstrumentStatusResponse response = instrumentService.checkInstrumentStatus(request);

        String message = response.isRecheckPerformed()
                ? "Instrument status checked with recheck performed"
                : "Instrument status retrieved successfully";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{instrumentId}/status")
    public ResponseEntity<ApiResponse<InstrumentStatusResponse>> getInstrumentStatus(@PathVariable String instrumentId) {
        CheckInstrumentStatusRequest request = new CheckInstrumentStatusRequest();
        request.setInstrumentId(instrumentId);
        request.setForceRecheck(false);

        return checkInstrumentStatus(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/activate")
    public ResponseEntity<ApiResponse<InstrumentActivationResponse>> activateInstrument(
            @Valid @RequestBody(required = false) ActivateInstrumentRequest request) {

        InstrumentActivationResponse response = instrumentService.activateInstrument(request);

        ApiResponse<InstrumentActivationResponse> apiResponse = ApiResponse.<InstrumentActivationResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Instrument activated successfully")
                .timestamp(LocalDateTime.now())
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/deactivate")
    public ResponseEntity<ApiResponse<InstrumentActivationResponse>> deactivateInstrument(
            @Valid @RequestBody(required = false) DeactivateInstrumentRequest request) {

        InstrumentActivationResponse response = instrumentService.deactivateInstrument(request);

        ApiResponse<InstrumentActivationResponse> apiResponse = ApiResponse.<InstrumentActivationResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Instrument deactivated successfully")
                .timestamp(LocalDateTime.now())
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<InstrumentResponse>>> getAllInstruments() {
        List<Instrument> instruments = instrumentService.getAllInstruments();

        if (instruments.isEmpty()) {
            // Trường hợp không có dữ liệu
            ApiResponse<List<InstrumentResponse>> apiResponse = ApiResponse.<List<InstrumentResponse>>builder()
                    .success(true) // Vẫn là success nhưng không có data
                    .status(HttpStatus.OK.value())
                    .message("No Data") // Thông báo "No Data"
                    .timestamp(LocalDateTime.now())
                    .data(null) // Không có data để trả về
                    .build();
            return ResponseEntity.ok(apiResponse);
        } else {
            // Chuyển đổi List<Instrument> thành List<InstrumentResponse>
            List<InstrumentResponse> responseDtos = instruments.stream()
                    .map(instrumentMapper::toResponse)
                    .collect(Collectors.toList());

            // Tạo ApiResponse thành công
            ApiResponse<List<InstrumentResponse>> apiResponse = ApiResponse.<List<InstrumentResponse>>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Instruments retrieved successfully.")
                    .timestamp(LocalDateTime.now())
                    .data(responseDtos)
                    .build();
            return ResponseEntity.ok(apiResponse);
        }
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<InstrumentResponse>>> getAllInstruments(
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

            @RequestParam(name = "configType", required = false)
            String configType,

            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // yyyy-MM-dd
            LocalDate startDate,

            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        PageResponse<InstrumentResponse> response = instrumentService.getAllInstruments(
                page, size, sort, search, configType, startDate, endDate
        );

        String message = response.getTotalElements() > 0
                ? "Instruments retrieved successfully."
                : "No Data";

        ApiResponse<PageResponse<InstrumentResponse>> apiResponse = ApiResponse.<PageResponse<InstrumentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
