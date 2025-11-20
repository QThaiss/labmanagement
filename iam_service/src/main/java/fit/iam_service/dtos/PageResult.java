/*
 * @ (#) PageResult.java    1.0    03/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.dtos;/*
 * @description:
 * @author: Bao Thong
 * @date: 03/10/2025
 * @version: 1.0
 */

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean empty
) {
    public static <T> PageResult<T> of(org.springframework.data.domain.Page<T> p) {
        return new PageResult<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(), p.isEmpty()
        );
    }
}