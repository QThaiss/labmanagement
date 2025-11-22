package fit.instrument_service.client.dtos;

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

    private LocalDateTime createdAt;

    List<TestParameterResponse> testParameters;
}