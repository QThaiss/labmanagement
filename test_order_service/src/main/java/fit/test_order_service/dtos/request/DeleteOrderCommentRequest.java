package fit.test_order_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteOrderCommentRequest {

    @NotBlank(message = "Delete reason cannot be blank")
    private String deleteReason;
}
