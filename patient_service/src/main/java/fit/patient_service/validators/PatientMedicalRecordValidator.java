/*
 * @ {#} PatientMedicalRecordValidator.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.validators;

import fit.patient_service.dtos.request.CreatePatientMedicalRecordRequest;
import fit.patient_service.dtos.request.UpdatePatientMedicalRecordRequest;
import fit.patient_service.enums.Gender;
import fit.patient_service.exceptions.GenderException;
import fit.patient_service.exceptions.NotEmptyException;
import fit.patient_service.exceptions.PatientMedicalRecordAlreadyExistsException;
import fit.patient_service.repositories.PatientMedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
 * @description: Validator for PatientMedicalRecord related operations
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class PatientMedicalRecordValidator {
    private final PatientMedicalRecordRepository patientMedicalRecordRepository;

    private final DateValidator dateValidator;

    public void validateCreatePatientMedicalRecordRequest(CreatePatientMedicalRecordRequest request) {
        // Validate date of birth
        dateValidator.validateDateOfBirth(request.dateOfBirth());

        // Validate email uniqueness
        if (request.email() != null && !request.email().trim().isEmpty()) {
            boolean emailExists = patientMedicalRecordRepository.existsByEmail(request.email().trim());
            if (emailExists) {
                throw new PatientMedicalRecordAlreadyExistsException("Email already exists");
            }
        }

        // Validate phone uniqueness
        if (request.phone() != null && !request.phone().trim().isEmpty()) {
            boolean phoneExists = patientMedicalRecordRepository.existsByPhone(request.phone().trim());
            if (phoneExists) {
                throw new PatientMedicalRecordAlreadyExistsException("Phone number already exists");
            }
        }

        // Check Gender validity
        try {
            Gender.valueOf(request.gender().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid gender: " + request.gender().toUpperCase() + ". Valid values are: MALE, FEMALE, OTHER");
        }
    }

    public void validateUpdatePatientMedicalRecordRequest(String medicalRecordCode, UpdatePatientMedicalRecordRequest request) {
        if (request.dateOfBirth() != null && !request.dateOfBirth().trim().isEmpty()) {
            dateValidator.validateDateOfBirth(request.dateOfBirth());
        }

        if (request.email() != null) {
            String email = request.email().trim();
            if (email.isEmpty()) {
                throw new NotEmptyException("Email must not be empty");
            }

            boolean exists = patientMedicalRecordRepository
                    .existsByEmailAndMedicalRecordCodeNot(email, medicalRecordCode);

            if (exists){
                throw new PatientMedicalRecordAlreadyExistsException("Email already exists");
            }
        }

        if (request.phone() != null && !request.phone().trim().isEmpty()) {
            boolean exists = patientMedicalRecordRepository
                    .existsByPhoneAndMedicalRecordCodeNot(request.phone().trim(), medicalRecordCode);
            if (exists){
                throw new PatientMedicalRecordAlreadyExistsException("Phone number already exists");
            }
        }

        if (request.gender() != null && !request.gender().trim().isEmpty()) {
            try {
                Gender.valueOf(request.gender().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new GenderException("Invalid gender: " + request.gender().toUpperCase() + ". Valid values are: MALE, FEMALE, OTHER");
            }
        }
    }

}
