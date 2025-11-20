package fit.warehouse_service.controllers;

import fit.warehouse_service.dtos.request.ReagentDeductionRequest;
import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.ReagentDeductionResponse;
import fit.warehouse_service.services.ReagentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/warehouse/reagents")
@RequiredArgsConstructor
public class ReagentController {

    private final ReagentService reagentService;

    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<ReagentDeductionResponse>> checkAndDeduct(
            @RequestBody ReagentDeductionRequest request) {
        ApiResponse<ReagentDeductionResponse> response = reagentService.checkAndDeductReagent(request);
        if (!response.getData().isDeductionSuccessful()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}