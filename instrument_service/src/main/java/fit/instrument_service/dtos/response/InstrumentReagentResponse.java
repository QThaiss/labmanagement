package fit.instrument_service.dtos.response;

import fit.instrument_service.embedded.Vendor;
import fit.instrument_service.enums.ReagentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InstrumentReagentResponse {
    private String id;
    private String instrumentId;
    private String reagentName;
    private String lotNumber;
    private Integer quantity;
    private LocalDate expirationDate;
    private ReagentStatus status;
    private Vendor vendor;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}