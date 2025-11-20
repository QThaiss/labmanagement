/*
 * @ {#} PatientMedicalRecordRepository.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.repositories;

import fit.patient_service.entities.PatientMedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/*
 * @description: Repository interface for managing PatientMedicalRecord entities
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
public interface PatientMedicalRecordRepository extends JpaRepository<PatientMedicalRecord, String>, JpaSpecificationExecutor<PatientMedicalRecord> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Page<PatientMedicalRecord> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<PatientMedicalRecord> findByMedicalRecordCode(String medicalRecordCode);

    Optional<PatientMedicalRecord> findByMedicalRecordId(String medicalRecordId);

    boolean existsByEmailAndMedicalRecordCodeNot(String email, String medicalRecordCode);

    boolean existsByPhoneAndMedicalRecordCodeNot(String phone, String medicalRecordCode);
}
