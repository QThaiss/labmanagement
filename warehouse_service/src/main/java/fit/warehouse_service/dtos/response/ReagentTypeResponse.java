package fit.warehouse_service.dtos.response;

import lombok.Builder;
import lombok.Data;

// Các trường lấy từ entity ReagentType
@Data
@Builder
public class ReagentTypeResponse {
    private String id;
    private String name;
    private String catalogNumber;
    private String manufacturer;
    private String casNumber;
    private String description;
    private String usagePerRun;
    private Boolean isDeleted;
}