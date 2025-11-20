package fit.test_order_service.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatedBySummary {
    private String userId;
    private String fullName;
}
