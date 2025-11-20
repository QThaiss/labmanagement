package fit.test_order_service.dtos.response;

import fit.test_order_service.client.dtos.TestParameterResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestTypeResponse {
    private String id;
    private String name;
    private String description;

    private String testParametersJson;

    private String reagentName;

    private double requiredVolume;

    List<TestParameterResponse> testParameters;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;
}