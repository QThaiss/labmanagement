/*
 * @ (#) RawTestResultController.java    1.0    29/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.controllers;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/11/2025
 * @version: 1.0
 */

import fit.instrument_service.dtos.request.DeleteRawResultRequest;
import fit.instrument_service.dtos.response.ApiResponse;
import fit.instrument_service.services.RawTestResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/instruments/raw-results")
@RequiredArgsConstructor
public class RawTestResultController {

    private final RawTestResultService rawTestResultService;

    // SRS 3.6.1.5: Manual Delete Raw Test Results
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteRawResults(@RequestBody DeleteRawResultRequest request) {
        rawTestResultService.deleteRawResults(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .success(true)
                .message("Selected raw results deleted successfully (only backed-up data).")
                .build());
    }
}
