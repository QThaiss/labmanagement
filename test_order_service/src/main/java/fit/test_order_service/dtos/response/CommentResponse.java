/*
 * @ (#) CommentResponse.java    1.0    04/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 04/11/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.CommentTargetType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private CommentTargetType targetType;
    private String testOrderId;
    private String testCode;
    private String resultId;
    private String testName;
    private String analyteName;
    private String resultValue;
}
