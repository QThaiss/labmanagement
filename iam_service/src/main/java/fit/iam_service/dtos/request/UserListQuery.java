/*
 * @ (#) UserListQuery.java    1.0    03/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 03/10/2025
 * @version: 1.0
 */

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record UserListQuery(
        // Tìm kiếm nhanh theo fullName/email/phone/identifyNumber
        String q,

        // Lọc theo gender: male/female (không phân biệt hoa thường)
        String gender,

        // Lọc theo tuổi
        Integer minAge,
        Integer maxAge,

        // Lọc theo ngày sinh (khoảng)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dobFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dobTo,

        // Phân trang + sort
        Integer page,      // default 0
        Integer size,      // default 20
        String sortBy,     // default "fullName"
        String sortDir     // "asc"/"desc", default "asc"
) {
}
