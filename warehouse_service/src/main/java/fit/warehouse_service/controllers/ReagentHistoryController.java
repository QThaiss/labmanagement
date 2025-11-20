package fit.warehouse_service.controllers;

import fit.warehouse_service.dtos.request.LogReagentUsageRequest;
import fit.warehouse_service.dtos.request.ReceiveReagentRequest;
import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.ReagentLotStatusResponse;
import fit.warehouse_service.dtos.response.ReagentSupplyHistoryResponse;
import fit.warehouse_service.dtos.response.ReagentUsageHistoryResponse;
import fit.warehouse_service.services.ReagentHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/warehouse/reagents/history")
@RequiredArgsConstructor
public class ReagentHistoryController {

    private final ReagentHistoryService reagentHistoryService;

    @PostMapping("/receive")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'LAB_USER')")
    public ResponseEntity<ApiResponse<ReagentSupplyHistoryResponse>> receiveReagent(
            @Valid @RequestBody ReceiveReagentRequest request) {

        ReagentSupplyHistoryResponse responseDto = reagentHistoryService.receiveReagentShipment(request);

        ApiResponse<ReagentSupplyHistoryResponse> apiResponse = ApiResponse.<ReagentSupplyHistoryResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())

                .message("Reagent shipment received and logged successfully.")
                .timestamp(LocalDateTime.now())
                .data(responseDto)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/supply")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'LAB_USER')")
    public ResponseEntity<ApiResponse<Page<ReagentSupplyHistoryResponse>>> getSupplyHistory(
            @RequestParam(required = false) String reagentTypeId,
            @RequestParam(required = false) String vendorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) { // Default sort by log time

        Page<ReagentSupplyHistoryResponse> historyPage = reagentHistoryService.getReagentSupplyHistory(
                reagentTypeId, vendorId, startDate, endDate, pageable);

        String message = historyPage.hasContent() ? "Reagent supply history retrieved successfully." : "No reagent supply history found matching criteria.";

        ApiResponse<Page<ReagentSupplyHistoryResponse>> apiResponse = ApiResponse.<Page<ReagentSupplyHistoryResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(historyPage)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/supply/{historyId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'LAB_USER')")
    public ResponseEntity<ApiResponse<ReagentSupplyHistoryResponse>> getSupplyHistoryById(@PathVariable String historyId) {
        ReagentSupplyHistoryResponse responseDto = reagentHistoryService.getReagentSupplyHistoryById(historyId);

        ApiResponse<ReagentSupplyHistoryResponse> apiResponse = ApiResponse.<ReagentSupplyHistoryResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Reagent supply history record retrieved successfully.")
                .timestamp(LocalDateTime.now())
                .data(responseDto)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/usage/log")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'LAB_USER')")
    public ResponseEntity<ApiResponse<ReagentUsageHistoryResponse>> logUsage(
            @Valid @RequestBody LogReagentUsageRequest request) {

        ReagentUsageHistoryResponse responseDto = reagentHistoryService.logReagentUsage(request);

        ApiResponse<ReagentUsageHistoryResponse> apiResponse = ApiResponse.<ReagentUsageHistoryResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Reagent usage logged successfully.")
                .timestamp(LocalDateTime.now())
                .data(responseDto)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/usage")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'LAB_USER')")
    public ResponseEntity<ApiResponse<Page<ReagentUsageHistoryResponse>>> getUsageHistory(
            @RequestParam(required = false) String reagentTypeId,
            @RequestParam(required = false) String reagentLotId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<ReagentUsageHistoryResponse> historyPage = reagentHistoryService.getReagentUsageHistory(reagentTypeId, reagentLotId, pageable);

        String message = historyPage.hasContent() ? "Reagent usage history retrieved successfully." : "No reagent usage history found matching criteria.";

        ApiResponse<Page<ReagentUsageHistoryResponse>> apiResponse = ApiResponse.<Page<ReagentUsageHistoryResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(historyPage)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
    @GetMapping("/validate-stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'LAB_USER')") // Giữ bảo mật tương tự các endpoint khác
    public ResponseEntity<Void> validateReagentStock(
            @RequestParam String vendorId,
            @RequestParam String lotNumber) {

        boolean exists = reagentHistoryService.checkReagentStockExists(vendorId, lotNumber);

        if (exists) {
            return ResponseEntity.ok().build(); // 200 OK - Tồn tại
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found - Không tồn tại
        }
    }
    @GetMapping("/lot-status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'LAB_USER')")
    public ResponseEntity<ApiResponse<ReagentLotStatusResponse>> getLotStatus(@RequestParam String lotNumber) {

        ReagentLotStatusResponse responseDto = reagentHistoryService.getReagentLotStatus(lotNumber);

        ApiResponse<ReagentLotStatusResponse> apiResponse = ApiResponse.<ReagentLotStatusResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Reagent lot status retrieved successfully.")
                .timestamp(LocalDateTime.now())
                .data(responseDto)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}