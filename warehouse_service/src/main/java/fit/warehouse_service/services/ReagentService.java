package fit.warehouse_service.services;

import fit.warehouse_service.dtos.request.ReagentDeductionRequest;
import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.ReagentDeductionResponse;


public interface ReagentService {
    ApiResponse<ReagentDeductionResponse> checkAndDeductReagent(ReagentDeductionRequest request);

    boolean checkReagentAvailability(String reagentName, Double requiredVolume);
}