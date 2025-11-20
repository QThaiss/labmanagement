package fit.test_order_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCommentRequest {
    @NotBlank(message = "Reply content cannot be blank")
    private String content;
}
