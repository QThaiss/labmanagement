package fit.warehouse_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogReagentUsageRequest {

    @NotBlank(message = "Reagent Lot ID cannot be blank.")
    private String reagentLotId;

    @NotBlank(message = "Instrument ID cannot be blank.")
    private String instrumentId;

    @NotNull(message = "Quantity Used cannot be null.")
    @Positive(message = "Quantity Used must be positive.")
    private Double quantityUsed;

    @NotBlank(message = "Action cannot be blank (e.g., 'USED', 'DISPOSED').")
    private String action;
}