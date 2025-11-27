/*
 * @ (#) OrderCommentResponse.java    1.0    13/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 13/10/2025
 * @version: 1.0
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCommentResponse {
    private String id;
    private String authorId;
    private CommentTargetResponse target;
    private String content;
    private LocalDateTime createdAt;
    private CreatedBySummary createdBy;
    private boolean edited;
    private Integer editCount;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private List<OrderCommentResponse> replies;
}