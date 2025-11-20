/*
 * @ {#} PatientMedicalRecord.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.entities;

import fit.patient_service.enums.Gender;
import fit.patient_service.utils.MedicalRecordGenerator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * @description: Entity representing the medical record of a patient
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@Entity
@Table(name = "patient_medical_records",
        indexes = {
                @Index(name = "idx_patient_code", columnList = "medical_record_code", unique = true),
                @Index(name = "idx_patient_name", columnList = "full_name"),
                @Index(name = "idx_patient_dob", columnList = "date_of_birth")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMedicalRecord extends Auditable{
    @Id
    @Column(name = "medical_record_id", length = 40)
    private String medicalRecordId;

    @Column(name = "medical_record_code", nullable = false, length = 50, unique = true)
    private String medicalRecordCode;

    @Column(name = "last_test_date")
    private LocalDateTime lastTestDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDateTime dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 120, unique = true)
    private String email;

    @Column(name = "address", length = 4096)
    private String address;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.medicalRecordId == null) {
            this.medicalRecordId = MedicalRecordGenerator.generateMedicalRecordId();
        }
        if (this.medicalRecordCode == null) {
            this.medicalRecordCode = MedicalRecordGenerator.generateMedicalRecordCode();
        }
    }
}
