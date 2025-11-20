/*
 * @ {#} TestOrderSpecification.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.specifications;

import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.enums.Gender;
import fit.test_order_service.enums.OrderStatus;
import fit.test_order_service.enums.ReviewMode;
import fit.test_order_service.enums.ReviewStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/*
 * @description: Specification class for building dynamic queries for TestOrder entity
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class TestOrderSpecification {

    public static final String SYSTEM_ORDER_ID = "SYSTEM_ORDER_ID";

    public Specification<TestOrder> build(String search, LocalDate startDate, LocalDate endDate,
                                          OrderStatus status, ReviewStatus reviewStatus, ReviewMode reviewMode,
                                          Gender gender, String createdBy, String reviewedBy) {
        Specification<TestOrder> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        // Exclude system-generated orders
        spec = spec.and((root, query, cb) -> cb.notEqual(root.get("orderId"), SYSTEM_ORDER_ID));

        if (StringUtils.hasText(search)) {
            Specification<TestOrder> searchSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("orderCode")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("phone")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("createdBy")), "%" + search.toLowerCase() + "%")
                    );
            spec = spec.and(searchSpec);
        }

        // Start date filtering (inclusive)
        if (startDate != null) {
            Specification<TestOrder> startDateSpec = (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay());
            spec = spec.and(startDateSpec);
        }

        // End date filtering (inclusive)
        if (endDate != null) {
            Specification<TestOrder> endDateSpec = (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59));
            spec = spec.and(endDateSpec);
        }

        // Status filtering
        if (status != null) {
            Specification<TestOrder> statusSpec = (root, query, cb) ->
                    cb.equal(root.get("status"), status);
            spec = spec.and(statusSpec);
        }

        // Review status filtering
        if (reviewStatus != null) {
            Specification<TestOrder> reviewStatusSpec = (root, query, cb) ->
                    cb.equal(root.get("reviewStatus"), reviewStatus);
            spec = spec.and(reviewStatusSpec);
        }

        // Review mode filtering
        if (reviewMode != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("reviewMode"), reviewMode));
        }

        // Gender filtering
        if (gender != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("gender"), gender));
        }

        // Created by filtering
        if (StringUtils.hasText(createdBy)) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("createdBy")), createdBy.toLowerCase()));
        }

        // Reviewed by filtering
        if (StringUtils.hasText(reviewedBy)) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("reviewedBy")), reviewedBy.toLowerCase()));
        }

        return spec;
    }
}
