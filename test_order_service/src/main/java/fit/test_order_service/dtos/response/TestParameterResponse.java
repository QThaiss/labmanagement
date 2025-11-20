package fit.test_order_service.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestParameterResponse {
    private String id;
    private String code;
    private String name;
    private String unit;
    private String referenceRange;
}