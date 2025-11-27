package fit.warehouse_service.dtos.request;

import lombok.Data;

@Data
public class ReagentInstallationDeductionRequest {
    private String lotNumber;
    private Double quantity; // Số lượng cần trừ (Install)
    private String instrumentId; // Để ghi log tracking
}