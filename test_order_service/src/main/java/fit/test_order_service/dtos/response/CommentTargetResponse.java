package fit.test_order_service.dtos.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentTargetResponse {

    private String type; // ORDER or RESULT

    private String testOrderId;

    private String resultId;
}
