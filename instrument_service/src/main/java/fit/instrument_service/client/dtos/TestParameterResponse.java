package fit.instrument_service.client.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TestParameterResponse {
    private String testParameterId;
    private String paramName;
    private String abbreviation;
    private String description;
    private LocalDateTime createdAt;
    private String createdByUserId;
    private LocalDateTime updatedAt;
    private String updatedByUserId;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
    private List<ParameterRangeResponse> parameterRanges;
}