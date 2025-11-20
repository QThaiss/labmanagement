package fit.test_order_service.client.dtos;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
public class ReagentDeductionRequest {
    @NotBlank
    private String reagentName;

    @NotNull
    @Min(1L)
    private Double requiredVolume;

    @NotBlank
    private String orderId; // Để tracking log ở service Warehouse
}