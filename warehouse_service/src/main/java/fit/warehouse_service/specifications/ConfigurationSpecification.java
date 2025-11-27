/*
 * @ {#} ConfigurationSpecification.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.specifications;

import fit.warehouse_service.entities.ConfigurationSetting;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

@Component
public class ConfigurationSpecification {

    public Specification<ConfigurationSetting> build(String search, String configType, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Search by Name or Instrument Model
            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.trim().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchLike);
                Predicate modelPredicate = cb.like(cb.lower(root.get("instrumentModel")), searchLike);
                predicates.add(cb.or(namePredicate, modelPredicate));
            }

            // 2. Filter by Config Type (General/Specific) - Thay cho DataType cũ
            if (StringUtils.hasText(configType)) {
                predicates.add(cb.equal(root.get("configType"), configType));
            }

            // 3. Filter by Date Range (createdAt)
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59)));
            }

            // Exclude deleted records
            predicates.add(cb.isNull(root.get("deletedAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
