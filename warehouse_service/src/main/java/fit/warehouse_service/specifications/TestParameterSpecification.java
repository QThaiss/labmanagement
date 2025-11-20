/*
 * @ {#} TestParameterSpecification.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.specifications;

import fit.warehouse_service.entities.TestParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/*
 * @description: Specification builder for TestParameter entity
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class TestParameterSpecification {

    public Specification<TestParameter> build(String search, LocalDate startDate, LocalDate endDate) {
        // Base specification to exclude soft-deleted records
        Specification<TestParameter> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));

        // Add general search conditions (paramName, abbreviation)
        if (StringUtils.hasText(search)) {
            Specification<TestParameter> searchSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("paramName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("abbreviation")), "%" + search.toLowerCase() + "%")
                    );
            spec = spec.and(searchSpec);
        }

        // Add condition for startDate of createdAt
        if (startDate != null) {
            Specification<TestParameter> startDateSpec = (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay());
            spec = spec.and(startDateSpec);
        }

        // Add condition for endDate of createdAt
        if (endDate != null) {
            Specification<TestParameter> endDateSpec = (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59));
            spec = spec.and(endDateSpec);
        }

        return spec;
    }
}
