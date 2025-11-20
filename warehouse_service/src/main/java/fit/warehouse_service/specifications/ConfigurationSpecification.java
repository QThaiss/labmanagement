/*
 * @ {#} ConfigurationSpecification.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.specifications;

import fit.warehouse_service.entities.ConfigurationSetting;
import fit.warehouse_service.enums.DataType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/*
 * @description: Specification builder for ConfigurationSetting entity
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
@Component
public class ConfigurationSpecification {

    public Specification<ConfigurationSetting> build(String search, DataType dataType, LocalDate startDate, LocalDate endDate) {
        // Loại bỏ các bản ghi đã bị xóa mềm
        Specification<ConfigurationSetting> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));

        // Tìm kiếm theo từ khóa trong tên hoặc mô tả
        if (StringUtils.hasText(search)) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")
                    ));
        }

        // Lọc theo dataType
        if (dataType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("dataType"), dataType));
        }

        // Lọc theo khoảng ngày tạo
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59)));
        }

        return spec;
    }
}
