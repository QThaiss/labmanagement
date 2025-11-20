package fit.iam_service.dtos.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record RoleListQuery(
        // tìm kiếm full-text roleName / roleCode / roleDescription
        String q,

        // lọc chính xác roleCode
        String roleCode,

        // lọc role hệ thống
        Boolean isSystem,

        // khoảng thời gian tạo
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdTo,

        // phân trang + sort
        Integer page,        // default 0
        Integer size,        // default 20
        String sortBy,       // default roleName
        String sortDir       // asc/desc
) {
}
