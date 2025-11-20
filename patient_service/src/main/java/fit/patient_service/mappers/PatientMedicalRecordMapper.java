/*
 * @ {#} PatientMedicalRecordMapper.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.mappers;

import fit.patient_service.dtos.request.CreatePatientMedicalRecordRequest;
import fit.patient_service.dtos.request.UpdatePatientMedicalRecordRequest;
import fit.patient_service.dtos.response.PatientMedicalRecordResponse;
import fit.patient_service.entities.PatientMedicalRecord;
import fit.patient_service.enums.Gender;
import fit.patient_service.utils.DateUtils;
import org.springframework.stereotype.Component;

/*
 * @description: Mapper class for PatientMedicalRecord entity and DTOs
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@Component
public class PatientMedicalRecordMapper {
    public PatientMedicalRecord toEntity(CreatePatientMedicalRecordRequest request) {
        if (request == null) {
            return null;
        }

        return PatientMedicalRecord.builder()
                .fullName(request.fullName().trim())
                .dateOfBirth(DateUtils.convertStringToLocalDateTime(request.dateOfBirth()))
                .gender(Gender.valueOf(request.gender().toUpperCase()))
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .notes(request.notes())
                .build();
    }

    public void applyUpdate(PatientMedicalRecord target, UpdatePatientMedicalRecordRequest request) {
        if (request.fullName() != null) {
            target.setFullName(request.fullName().trim());
        }
        if (request.dateOfBirth() != null) {
            target.setDateOfBirth(DateUtils.convertStringToLocalDateTime(request.dateOfBirth()));
        }

        if(request.gender()!=null) {
            target.setGender(Gender.valueOf(request.gender().toUpperCase()));
        }

        if (request.phone() != null) {
            target.setPhone(request.phone());
        }

        if (request.email() != null) {
            target.setEmail(request.email());
        }

        if (request.address() != null) {
            target.setAddress(request.address());
        }

        if (request.notes() != null) {
            target.setNotes(request.notes());
        }
    }

    public PatientMedicalRecordResponse toResponse(PatientMedicalRecord entity) {
        if (entity == null) {
            return null;
        }

        return new PatientMedicalRecordResponse(
                entity.getMedicalRecordId(),
                entity.getMedicalRecordCode(),
                entity.getFullName(),
                entity.getDateOfBirth(),
                entity.getGender(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getNotes(),
                entity.getLastTestDate(),
                DateUtils.toVietnamTime(entity.getCreatedAt()),
                entity.getCreatedBy(),
                DateUtils.toVietnamTime(entity.getUpdatedAt()),
                entity.getUpdatedBy(),
                DateUtils.toVietnamTime(entity.getDeletedAt())
        );
    }
}
