/*
 * @ {#} InternalPatientMedicalRecordController.java   1.0     15/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.internal_controllers;

import fit.patient_service.constants.RoleConstants;
import fit.patient_service.dtos.response.ApiResponse;
import fit.patient_service.dtos.response.PatientMedicalRecordResponse;
import fit.patient_service.services.PatientMedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @description: Controller for internal patient medical record operations
 * @author: Tran Hien Vinh
 * @date:   15/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/internal/patient-medical-records")
@RequiredArgsConstructor
public class InternalPatientMedicalRecordController {
    private final PatientMedicalRecordService patientMedicalRecordService;

    @GetMapping("/code/{medicalRecordCode}")
    @PreAuthorize(RoleConstants.CAN_READ_PMR)
    public ResponseEntity<ApiResponse<PatientMedicalRecordResponse>> getPatientMedicalRecordByCode(@PathVariable String medicalRecordCode) {

        PatientMedicalRecordResponse response =  patientMedicalRecordService.getPatientMedicalRecordByCode(medicalRecordCode);

        return ResponseEntity.ok(ApiResponse.success(response, "Patient medical record retrieved successfully"));
    }

    @GetMapping("/{medicalRecordId}")
    @PreAuthorize(RoleConstants.CAN_READ_PMR)
    public ResponseEntity<ApiResponse<PatientMedicalRecordResponse>> getPatientMedicalRecordById(@PathVariable String medicalRecordId) {

        PatientMedicalRecordResponse response =  patientMedicalRecordService.getPatientMedicalRecordById(medicalRecordId);

        return ResponseEntity.ok(ApiResponse.success(response, "Patient medical record retrieved successfully"));
    }
}
