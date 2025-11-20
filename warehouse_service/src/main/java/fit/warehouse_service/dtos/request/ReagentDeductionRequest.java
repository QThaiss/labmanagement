package fit.warehouse_service.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReagentDeductionRequest {
    private String reagentName;
    private double requiredVolume;
    private String orderId; // Để log lại usage cho order nào
}