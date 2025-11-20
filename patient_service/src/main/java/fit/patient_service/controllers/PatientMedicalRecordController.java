/*
 * @ {#} PatientMedicalRecordController.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.controllers;

import fit.patient_service.constants.RoleConstants;
import fit.patient_service.dtos.request.CreatePatientMedicalRecordRequest;
import fit.patient_service.dtos.request.UpdatePatientMedicalRecordRequest;
import fit.patient_service.dtos.response.ApiResponse;
import fit.patient_service.dtos.response.PageResponse;
import fit.patient_service.dtos.response.PatientMedicalRecordResponse;
import fit.patient_service.enums.Gender;
import fit.patient_service.services.PatientMedicalRecordService;
import fit.patient_service.validators.RequestParamValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/*
 * @description: Controller for managing patient medical records
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/patient-medical-records")
@RequiredArgsConstructor
@Validated
public class PatientMedicalRecordController {
    private final PatientMedicalRecordService patientMedicalRecordService;

    @PostMapping
    @PreAuthorize(RoleConstants.CAN_CRUD_PMR)
    public ResponseEntity<ApiResponse<PatientMedicalRecordResponse>> createPatientMedicalRecord(
            @Valid @RequestBody CreatePatientMedicalRecordRequest request) {

        PatientMedicalRecordResponse response = patientMedicalRecordService.createPatientMedicalRecord(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Patient medical record created successfully"));
    }

    @PutMapping("/{medicalRecordCode}")
    @PreAuthorize(RoleConstants.CAN_CRUD_PMR)
    public ResponseEntity<ApiResponse<PatientMedicalRecordResponse>> updatePatientMedicalRecord(
            @PathVariable String medicalRecordCode,
            @Valid @RequestBody UpdatePatientMedicalRecordRequest request) {

        PatientMedicalRecordResponse response = patientMedicalRecordService.updatePatientMedicalRecord(medicalRecordCode, request);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Patient medical record updated successfully"));
    }

    @DeleteMapping("/{medicalRecordCode}")
    @PreAuthorize(RoleConstants.CAN_CRUD_PMR)
    public ResponseEntity<ApiResponse<Void>> deletePatientMedicalRecord(@PathVariable String medicalRecordCode) {

        patientMedicalRecordService.softDeletePatientMedicalRecord(medicalRecordCode);

        return ResponseEntity
                .ok(ApiResponse.noContent("Patient medical record deleted successfully"));
    }

    @GetMapping
    @PreAuthorize(RoleConstants.CAN_READ_PMR)
    public ResponseEntity<ApiResponse<PageResponse<PatientMedicalRecordResponse>>> getAllPatientMedicalRecords(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must not be less than zero")
            int page,

            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "Page size must not be less than one")
            int size,

            @RequestParam(name = "sort", required = false)
            String[] sort,

            @RequestParam(name = "search", required = false)
            String search,

            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // yyyy-MM-dd
            LocalDate startDate,

            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // yyyy-MM-dd
            LocalDate endDate,

            @RequestParam(name = "gender", required = false)
            Gender gender
    ) {
        PageResponse<PatientMedicalRecordResponse> response = patientMedicalRecordService.getAllPatientMedicalRecords(page, size, sort, search, startDate, endDate, gender);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Fetched patient medical records successfully"));
    }

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
