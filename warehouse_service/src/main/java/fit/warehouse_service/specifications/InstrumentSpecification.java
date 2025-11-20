/*
 * @ {#} InstrumentSpecification.java   1.0     04/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.specifications;

import fit.warehouse_service.entities.Instrument;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class InstrumentSpecification {

    public Specification<Instrument> build(String search, LocalDate startDate, LocalDate endDate) {
        // Luôn bắt đầu bằng việc loại bỏ các bản ghi đã xóa mềm
        Specification<Instrument> spec = (root, query, cb) -> cb.equal(root.get("isDeleted"), false);

        // Tìm kiếm theo từ khóa (trong tên hoặc địa chỉ IP)
        if (StringUtils.hasText(search)) {
            String searchLower = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), searchLower),
                            cb.like(cb.lower(root.get("ipAddress")), searchLower)
                    ));
        }

        // Lọc theo khoảng ngày tạo (createdAt)
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(LocalTime.MAX)));
        }

        return spec;
    }
}