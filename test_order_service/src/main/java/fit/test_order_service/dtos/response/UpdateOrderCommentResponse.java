package fit.test_order_service.dtos.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderCommentResponse {
    private String commentId;
    private String content;
    private boolean edited;
    private Integer editCount;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
