package fit.test_order_service.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateTestTypeRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    // Danh sách ID lấy từ warehouse
    private List<String> testParameterIds;

    @NotBlank(message = "Reagent name is required")
    private String reagentName;

    @Min(value = 0, message = "Required volume must be positive")
    private long requiredVolume;
}