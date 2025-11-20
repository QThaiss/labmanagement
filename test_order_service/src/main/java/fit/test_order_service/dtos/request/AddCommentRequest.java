package fit.test_order_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCommentRequest {

    @NotBlank(message = "Target ID cannot be blank")
    private String targetId;

    @NotBlank(message = "Target type cannot be blank")
    @Pattern(regexp = "^(ORDER|RESULT)$", message = "Target type must be 'ORDER' or 'RESULT'")
    private String targetType;

    @NotBlank(message = "Comment content cannot be blank")
    private String content;
}
