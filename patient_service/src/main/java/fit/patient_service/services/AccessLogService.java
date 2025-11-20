/*
 * @ {#} AccessLogService.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.services;

import fit.patient_service.entities.PatientMedicalRecord;

import java.util.List;

/*
 * @description: Service interface for logging access actions on Patient entities
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
public interface AccessLogService {
    void logCreatePatientMedicalRecord(PatientMedicalRecord patientMedicalRecord);

    void logUpdatePatientMedicalRecord(PatientMedicalRecord before, PatientMedicalRecord after);

    void logDeletePatientMedicalRecord(PatientMedicalRecord patientMedicalRecord);

    void logGetAllPatientMedicalRecords(int page, int size, long totalElements, List<String> sorts);

    void logGetPatientMedicalRecordByCode(PatientMedicalRecord patientMedicalRecord);

    void logGetPatientMedicalRecordById(PatientMedicalRecord patientMedicalRecord);
}
