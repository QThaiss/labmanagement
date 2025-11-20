package fit.instrument_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ModifyReagentStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "IN_USE|NOT_IN_USE", message = "Status must be 'IN_USE' or 'NOT_IN_USE'")
    private String status;
}