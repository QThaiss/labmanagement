/*
 * @ {#} Hl7ProcessingController.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.controllers;

import fit.test_order_service.dtos.request.Hl7MessageRequest;
import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.dtos.response.Hl7ProcessResponse;
import fit.test_order_service.services.Hl7OrderSenderService;
import fit.test_order_service.services.Hl7ProcessingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
/*
 * @description: REST controller for HL7 message processing
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/hl7")
@RequiredArgsConstructor
@Validated
@Slf4j
public class Hl7ProcessingController {

    private final Hl7ProcessingService hl7ProcessingService;

    private final Hl7OrderSenderService hl7OrderSenderService;

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Hl7ProcessResponse>> processHl7Message(
            @Valid @RequestBody Hl7MessageRequest request) {

        Hl7ProcessResponse response = hl7ProcessingService.processHl7Message(request);
        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Hl7ProcessResponse>builder()
                            .success(false)
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("HL7 message processing failed: " + response.getErrorMessage())
                            .data(response)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<Hl7ProcessResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("HL7 message processed successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/{testOrderId}/request")
    public ResponseEntity<ApiResponse<Hl7ProcessResponse>> sendOrderToInstrument(
            @PathVariable String testOrderId
    ) {
        log.info("[API] Sending HL7 order request for TestOrder ID: {}", testOrderId);
        Hl7ProcessResponse response = hl7OrderSenderService.sendOrderAndProcessResult(testOrderId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "HL7 order request built successfully")
        );
    }
}
