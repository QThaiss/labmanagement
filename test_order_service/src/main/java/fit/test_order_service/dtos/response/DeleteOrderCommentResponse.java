package fit.test_order_service.dtos.response;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteOrderCommentResponse {
    private String commentId;
    private boolean deleted;
    private String deletedBy;
    private LocalDateTime deletedAt;
    private String deleteReason;
}
