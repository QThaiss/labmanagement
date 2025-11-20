/*
 * @ {#} InstrumentController.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.controllers;

import fit.instrument_service.dtos.request.ChangeInstrumentModeRequest;
import fit.instrument_service.dtos.request.InstallReagentRequest;
import fit.instrument_service.dtos.request.ModifyReagentStatusRequest;
import fit.instrument_service.dtos.response.ApiResponse;
import fit.instrument_service.dtos.response.InstrumentReagentResponse;
import fit.instrument_service.dtos.response.InstrumentResponse;
import fit.instrument_service.services.InstrumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/*
 * @description: Controller for managing Instruments.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/instruments")
@RequiredArgsConstructor
public class InstrumentController {
    private final InstrumentService instrumentService;

    @PatchMapping("/{instrumentId}/mode")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<InstrumentResponse>> changeInstrumentMode(
            @PathVariable String instrumentId,
            @Valid @RequestBody ChangeInstrumentModeRequest request) {

        InstrumentResponse updatedInstrument = instrumentService.changeInstrumentMode(instrumentId, request);

        return ResponseEntity.ok(ApiResponse.success(updatedInstrument,
                "Instrument mode updated successfully"));
    }
    @PostMapping("/{instrumentId}/reagents")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'LAB_USER')") // Điều chỉnh quyền theo yêu cầu
    public ResponseEntity<ApiResponse<InstrumentReagentResponse>> installReagent(
            @PathVariable String instrumentId,
            @Valid @RequestBody InstallReagentRequest request) {

        InstrumentReagentResponse newReagent = instrumentService.installReagent(instrumentId, request);

        return new ResponseEntity<>(
                ApiResponse.success(newReagent, "Reagent installed on instrument successfully"),
                HttpStatus.CREATED
        );
    }
//    @PatchMapping("/{instrumentId}/reagents/{reagentId}/status")
//    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'LAB_USER')") // Điều chỉnh quyền theo yêu cầu
//    public ResponseEntity<ApiResponse<InstrumentReagentResponse>> modifyReagentStatus(
//            @PathVariable String instrumentId,
//            @PathVariable String reagentId,
//            @Valid @RequestBody ModifyReagentStatusRequest request) {
//
//        InstrumentReagentResponse updatedReagent = instrumentService.modifyReagentStatus(instrumentId, reagentId, request);
//
//        return ResponseEntity.ok(
//                ApiResponse.success(updatedReagent, "Reagent status updated successfully")
//        );
//    }
}
