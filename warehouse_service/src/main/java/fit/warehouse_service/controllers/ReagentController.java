package fit.warehouse_service.controllers;

import fit.warehouse_service.dtos.request.ReagentDeductionRequest;
import fit.warehouse_service.dtos.request.ReagentInstallationDeductionRequest;
import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.ReagentDeductionResponse;
import fit.warehouse_service.services.ReagentService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/check-availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_TECHNICIAN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Boolean>> checkReagentAvailability(
            @RequestParam @NotBlank(message = "Reagent name is required") String reagentName,
            @RequestParam @NotNull(message = "Required volume is required") @Positive(message = "Required volume must be positive") Double requiredVolume) {

        boolean isAvailable = reagentService.checkReagentAvailability(reagentName, requiredVolume);

        String message = isAvailable ? "Reagent available" : "Reagent not available";

        return ResponseEntity.ok(ApiResponse.success(isAvailable, message));
    }

    @PostMapping("/deduct-installation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Tùy chỉnh role
    public ResponseEntity<ApiResponse<Boolean>> deductForInstallation(
            @RequestBody ReagentInstallationDeductionRequest request) {
        return ResponseEntity.ok(reagentService.deductReagentForInstallation(request));
    }

}