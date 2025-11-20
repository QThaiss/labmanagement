/*
 * @ (#) CommentOrderResponse.java    1.0    04/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 04/11/2025
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
public class CommentOrderResponse {
    private String id;
    private CommentAuthorResponse author;

    private CommentResponse targetInfo;

    private String content;
    private LocalDateTime createdAt;
    private boolean edited;
    private LocalDateTime updatedAt;

    private List<CommentOrderResponse> replies;
}
