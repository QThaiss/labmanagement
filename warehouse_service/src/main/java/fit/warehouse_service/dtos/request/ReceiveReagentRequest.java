package fit.warehouse_service.dtos.request;

import fit.warehouse_service.enums.SupplyStatus;
import jakarta.validation.constraints.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReceiveReagentRequest {

    // Reagent Info (ID of existing ReagentType)
    @NotBlank(message = "Reagent Type ID cannot be blank.")
    private String reagentTypeId;

    @NotBlank(message = "Vendor ID cannot be blank.")
    private String vendorId;

    // Supply/Order Details
    @NotBlank(message = "PO Number cannot be blank.")
    private String poNumber;
    @NotNull(message = "Order Date cannot be null.")
    private LocalDate orderDate;
    @NotNull(message = "Receipt Date cannot be null.")
    private LocalDateTime receiptDate; // User-provided receipt date/time

    @NotNull(message = "Quantity Received cannot be null.")
    @Positive(message = "Quantity Received must be positive.")
    private Double quantityReceived;
    @NotBlank(message = "Unit of Measure cannot be blank.")
    private String unitOfMeasure;

    // Batch Information
    @NotBlank(message = "Lot Number cannot be blank.")
    private String lotNumber;
    @NotNull(message = "Expiration Date cannot be null.")
    @FutureOrPresent(message = "Expiration Date must be in the present or future.")
    private LocalDate expirationDate;

    @NotBlank(message = "Initial Storage Location cannot be blank.")
    private String initialStorageLocation;

    // Status
    @NotNull(message = "Supply Status cannot be null.")
    private SupplyStatus status;
}