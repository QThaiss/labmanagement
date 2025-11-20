/*
 * @ {#} PatientMedicalRecordService.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.services;

import fit.patient_service.dtos.request.CreatePatientMedicalRecordRequest;
import fit.patient_service.dtos.request.UpdatePatientMedicalRecordRequest;
import fit.patient_service.dtos.response.PageResponse;
import fit.patient_service.dtos.response.PatientMedicalRecordResponse;
import fit.patient_service.enums.Gender;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/*
 * @description: Service class for managing patient medical records
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@Repository
public interface PatientMedicalRecordService {
    PatientMedicalRecordResponse createPatientMedicalRecord(CreatePatientMedicalRecordRequest request);

    PatientMedicalRecordResponse updatePatientMedicalRecord(String medicalRecordCode, UpdatePatientMedicalRecordRequest request);

    void softDeletePatientMedicalRecord(String medicalRecordCode);

    PageResponse<PatientMedicalRecordResponse> getAllPatientMedicalRecords(int page, int size, String[] sort, String search, LocalDate startDate, LocalDate endDate, Gender gender);

    PatientMedicalRecordResponse getPatientMedicalRecordByCode(String medicalRecordCode);

    PatientMedicalRecordResponse getPatientMedicalRecordById(String medicalRecordId);
}
