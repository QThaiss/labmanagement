package fit.warehouse_service.dtos.response;

import fit.warehouse_service.enums.SupplyStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReagentSupplyHistoryResponse {

    private String id; // History record ID

    // Reagent Information
    private String reagentTypeId;
    private String reagentName;
    private String reagentCatalogNumber;
    private String reagentManufacturer;
    private String reagentCasNumber;

    // Vendor Details
    private String vendorName;
    private String vendorId;

    // Supply/Order Details
    private String poNumber;
    private LocalDate orderDate;
    private LocalDateTime receiptDate; // User entered receipt date
    private double quantityReceived;
    private String unitOfMeasure;

    // Batch Information
    private String lotNumber;
    private LocalDate expirationDate;

    // Receiving Information
    private String receivedByUserId; // Explicit user who received
    private String loggedByUserId; // User who created the record (from BaseEntity)
    private LocalDateTime loggedAt; // Timestamp when record was created (from BaseEntity)
    private String initialStorageLocation;

    // Status
    private SupplyStatus status;
}