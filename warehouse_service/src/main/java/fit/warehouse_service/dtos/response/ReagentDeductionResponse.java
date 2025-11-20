package fit.warehouse_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReagentDeductionResponse {
    private boolean deductionSuccessful;
    private String message;
    private double deductedVolume;
}