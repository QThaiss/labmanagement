package fit.test_order_service.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Không import List vì không dùng nữa

@Data
public class UpdateTestTypeRequest {

    private String name;

    private String description;

    private String reagentName;

    @Min(value = 0, message = "Required volume must be positive")
    private Double requiredVolume;
}