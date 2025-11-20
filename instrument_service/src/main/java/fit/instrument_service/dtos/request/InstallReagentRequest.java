package fit.instrument_service.dtos.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InstallReagentRequest {

    @NotBlank(message = "Vendor ID is required")
    private String vendorId;

    @NotBlank(message = "Reagent name is required")
    private String reagentName;

    @NotBlank(message = "Lot number is required")
    private String lotNumber;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotNull(message = "Expiration date is required")
    @FutureOrPresent(message = "Expiration date must be in the present or future")
    private LocalDate expirationDate;

}