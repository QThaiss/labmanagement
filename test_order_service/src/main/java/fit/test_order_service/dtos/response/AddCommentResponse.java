package fit.test_order_service.dtos.response;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCommentResponse {
    private String commentId;
    private CommentTargetResponse target;
    private String content;
    private CreatedBySummary createdBy;
    private LocalDateTime createdAt;


}
