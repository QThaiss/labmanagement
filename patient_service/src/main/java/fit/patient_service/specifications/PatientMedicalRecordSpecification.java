/*
 * @ {#} PatientMedicalRecordSpecification.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.specifications;

import fit.patient_service.entities.PatientMedicalRecord;
import fit.patient_service.enums.Gender;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/*
 * @description: Specification builder for PatientMedicalRecord entity
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */

@Component
public class PatientMedicalRecordSpecification {

    /**
     * Builds a dynamic Specification for PatientMedicalRecord based on filter criteria.
     *
     * @param search     A general search term for fullName or medicalRecordNumber.
     * @param startDate  The start date for filtering (inclusive).
     * @param endDate    The end date for filtering (inclusive).
     * @param gender     Gender for filtering.
     * @return The combined Specification object.
     */
    public Specification<PatientMedicalRecord> build(String search, LocalDate startDate, LocalDate endDate, Gender gender) {
        // Base specification to exclude soft-deleted records
        Specification<PatientMedicalRecord> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));

        // Add general search conditions (fullName, medicalRecordCode)
        if (StringUtils.hasText(search)) {
            // Search in fullName and medicalRecordCode fields
            Specification<PatientMedicalRecord> searchSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("medicalRecordCode")), "%" + search.toLowerCase() + "%")
                    );
            spec = spec.and(searchSpec);
        }

        // Add condition for startDate of createdAt
        if (startDate != null) {
            Specification<PatientMedicalRecord> startDateSpec = (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay());
            spec = spec.and(startDateSpec);
        }

        // Add condition for endDate of createdAt
        if (endDate != null) {
            Specification<PatientMedicalRecord> endDateSpec = (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59));
            spec = spec.and(endDateSpec);
        }

        // Add condition for gender
        if (gender != null) {
            Specification<PatientMedicalRecord> genderSpec = (root, query, cb) ->
                    cb.equal(root.get("gender"), gender);
            spec = spec.and(genderSpec);
        }

        return spec;
    }
}
