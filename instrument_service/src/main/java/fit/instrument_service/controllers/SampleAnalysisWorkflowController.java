/*
 * @ {#} SampleAnalysisWorkflowController.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.controllers;

import fit.instrument_service.dtos.request.InitiateWorkflowRequest;
import fit.instrument_service.dtos.response.ApiResponse;
import fit.instrument_service.dtos.response.SampleResponse;
import fit.instrument_service.dtos.response.WorkflowResponse;
import fit.instrument_service.services.SampleAnalysisWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * @description: Controller for managing sample analysis workflows
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/sample-analysis")
@RequiredArgsConstructor
@Slf4j
public class SampleAnalysisWorkflowController {

    private final SampleAnalysisWorkflowService workflowService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<WorkflowResponse>> initiateWorkflow(
            @Valid @RequestBody InitiateWorkflowRequest request) {
        log.info("Received workflow initiation request for instrument: {}", request.getInstrumentId());
        WorkflowResponse response = workflowService.initiateWorkflow(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Workflow initiated successfully"));
    }

    @PostMapping("/process-next/{instrumentId}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> processNextCassette(
            @PathVariable String instrumentId) {
        log.info("Processing next cassette for instrument: {}", instrumentId);
        WorkflowResponse response = workflowService.processNextCassette(instrumentId);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.noContent("No cassettes in queue"));
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Next cassette processing initiated"));
    }

    @GetMapping("/workflow/{workflowId}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> getWorkflowStatus(
            @PathVariable String workflowId) {
        log.info("Getting workflow status for: {}", workflowId);
        WorkflowResponse response = workflowService.getWorkflowStatus(workflowId);
        return ResponseEntity.ok(ApiResponse.success(response, "Workflow status retrieved"));
    }

    @GetMapping("/workflow/{workflowId}/samples")
    public ResponseEntity<ApiResponse<List<SampleResponse>>> getWorkflowSamples(
            @PathVariable String workflowId) {
        log.info("Getting samples for workflow: {}", workflowId);
        List<SampleResponse> samples = workflowService.getWorkflowSamples(workflowId);
        return ResponseEntity.ok(ApiResponse.success(samples, "Workflow samples retrieved"));
    }
}
