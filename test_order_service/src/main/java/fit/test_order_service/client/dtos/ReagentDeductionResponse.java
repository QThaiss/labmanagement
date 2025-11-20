package fit.test_order_service.client.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReagentDeductionResponse {
    private boolean deductionSuccessful;
    private String orderId;
    private String message;
}