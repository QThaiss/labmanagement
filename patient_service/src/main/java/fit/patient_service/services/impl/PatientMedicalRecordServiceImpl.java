/*
 * @ {#} PatientMedicalRecordServiceImpl.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.services.impl;

import fit.patient_service.client.IamFeignClient;
import fit.patient_service.client.dtos.UserInternalResponse;
import fit.patient_service.constants.SortFields;
import fit.patient_service.dtos.request.CreatePatientMedicalRecordRequest;
import fit.patient_service.dtos.request.UpdatePatientMedicalRecordRequest;
import fit.patient_service.dtos.response.ApiResponse;
import fit.patient_service.dtos.response.FilterInfo;
import fit.patient_service.dtos.response.PageResponse;
import fit.patient_service.dtos.response.PatientMedicalRecordResponse;
import fit.patient_service.entities.PatientMedicalRecord;
import fit.patient_service.enums.Gender;
import fit.patient_service.exceptions.NotFoundException;
import fit.patient_service.mappers.PatientMedicalRecordMapper;
import fit.patient_service.repositories.PatientMedicalRecordRepository;
import fit.patient_service.services.AccessLogService;
import fit.patient_service.services.PatientMedicalRecordService;
import fit.patient_service.specifications.PatientMedicalRecordSpecification;
import fit.patient_service.utils.SecurityUtils;
import fit.patient_service.utils.SortUtils;
import fit.patient_service.validators.PatientMedicalRecordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * @description: Service implementation for managing patient medical records
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class PatientMedicalRecordServiceImpl implements PatientMedicalRecordService {
    private final PatientMedicalRecordRepository patientMedicalRecordRepository;

    private final PatientMedicalRecordValidator patientMedicalRecordValidator;

    private final PatientMedicalRecordMapper patientMedicalRecordMapper;

    private final IamFeignClient iamFeignClient;

    private final AccessLogService accessLogService;

    private final PatientMedicalRecordSpecification specificationBuilder;

    @Transactional
    @Override
    public PatientMedicalRecordResponse createPatientMedicalRecord(CreatePatientMedicalRecordRequest request) {
        // Validate request
        patientMedicalRecordValidator.validateCreatePatientMedicalRecordRequest(request);

        // Get current authenticated user
        String currentUserId = SecurityUtils.getCurrentUserId();

        // Set createdBy field
        ApiResponse<UserInternalResponse> response = iamFeignClient.getUserById(currentUserId);
        UserInternalResponse creator = response.getData();

        // Map to entity
        PatientMedicalRecord entity = patientMedicalRecordMapper.toEntity(request);
        entity.setCreatedBy(creator.userId());

        // Save entity
        PatientMedicalRecord savedEntity = patientMedicalRecordRepository.save(entity);

        // Log creation action
        accessLogService.logCreatePatientMedicalRecord(savedEntity);

        // Map to response
        return patientMedicalRecordMapper.toResponse(savedEntity);
    }

    @Transactional
    @Override
    public PatientMedicalRecordResponse updatePatientMedicalRecord(String medicalRecordCode, UpdatePatientMedicalRecordRequest request) {
        // Check existence
        PatientMedicalRecord entity = patientMedicalRecordRepository.findByMedicalRecordCode(medicalRecordCode)
                .orElseThrow(() -> new NotFoundException(NotFoundException.PATIENT_MEDICAL_RECORD_NOT_FOUND + medicalRecordCode));

        // Snapshot before update for auditing
        PatientMedicalRecord before = snapshot(entity);

        // Validate
        patientMedicalRecordValidator.validateUpdatePatientMedicalRecordRequest(medicalRecordCode, request);

        // Set updatedBy (via IAM for consistency)
        String currentUserId = SecurityUtils.getCurrentUserId();
        ApiResponse<UserInternalResponse> response = iamFeignClient.getUserById(currentUserId);
        UserInternalResponse updater = response.getData();

        // Apply update
        patientMedicalRecordMapper.applyUpdate(entity, request);
        entity.setUpdatedBy(updater.userId());

        PatientMedicalRecord saved = patientMedicalRecordRepository.save(entity);

        // Log update
        accessLogService.logUpdatePatientMedicalRecord(before, saved);

        return patientMedicalRecordMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public void softDeletePatientMedicalRecord(String medicalRecordCode) {
        // Check existence
        PatientMedicalRecord entity = patientMedicalRecordRepository.findByMedicalRecordCode(medicalRecordCode)
                .orElseThrow(() -> new NotFoundException(NotFoundException.PATIENT_MEDICAL_RECORD_NOT_FOUND + medicalRecordCode));

        // If already deleted, do nothing
        if (entity.getDeletedAt() != null) return;

        // Set deletedAt and updatedBy
        String currentUserId = SecurityUtils.getCurrentUserId();
        entity.setUpdatedBy(currentUserId);
        entity.setDeletedAt(LocalDateTime.now());

        PatientMedicalRecord saved = patientMedicalRecordRepository.save(entity);

        accessLogService.logDeletePatientMedicalRecord(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PatientMedicalRecordResponse> getAllPatientMedicalRecords(int page, int size, String[] sort, String search, LocalDate startDate, LocalDate endDate, Gender gender) {
        // Validate and build Sort object
        Sort validSort = SortUtils.buildSort(
                sort,
                SortFields.PATIENT_MEDICAL_RECORD_SORT_FIELDS,
                SortFields.DEFAULT_PATIENT_MEDICAL_RECORD_SORT
        );

        // Create Pageable
        Pageable pageable = PageRequest.of(page, size, validSort);

        Specification<PatientMedicalRecord> spec = specificationBuilder.build(search, startDate, endDate, gender);

        // Query non-deleted PMRs with pagination
        Page<PatientMedicalRecord> pmrPage = patientMedicalRecordRepository.findAll(spec,pageable);

        // Map to response DTOs
        Page<PatientMedicalRecordResponse> dtoPage = pmrPage.map(patientMedicalRecordMapper::toResponse);

        // Create FilterInfo
        FilterInfo filterInfo = FilterInfo.builder()
                .search(search)
                .startDate(startDate)
                .endDate(endDate)
                .gender(gender)
                .build();

        // Create PageResponse
        PageResponse<PatientMedicalRecordResponse> pageResponse = PageResponse.from(dtoPage, filterInfo);

        // Log the access action
        accessLogService.logGetAllPatientMedicalRecords(page, size, pmrPage.getTotalElements(), pageResponse.getSorts());

        return pageResponse;
    }

    @Transactional(readOnly = true)
    @Override
    public PatientMedicalRecordResponse getPatientMedicalRecordByCode(String medicalRecordCode) {
        PatientMedicalRecord entity = patientMedicalRecordRepository.findByMedicalRecordCode(medicalRecordCode)
                .orElseThrow(() -> new NotFoundException(NotFoundException.PATIENT_MEDICAL_RECORD_NOT_FOUND + medicalRecordCode));

        accessLogService.logGetPatientMedicalRecordByCode(entity);

        return patientMedicalRecordMapper.toResponse(entity);
    }

    @Override
    public PatientMedicalRecordResponse getPatientMedicalRecordById(String medicalRecordId) {
        PatientMedicalRecord entity = patientMedicalRecordRepository.findByMedicalRecordId(medicalRecordId)
                .orElseThrow(() -> new NotFoundException(NotFoundException.PATIENT_MEDICAL_RECORD_NOT_FOUND + medicalRecordId));

        accessLogService.logGetPatientMedicalRecordById(entity);

        return patientMedicalRecordMapper.toResponse(entity);
    }

    // Create a minimal snapshot for diff logging
    private PatientMedicalRecord snapshot(PatientMedicalRecord src) {
        PatientMedicalRecord copy = new PatientMedicalRecord();
        copy.setMedicalRecordId(src.getMedicalRecordId());
        copy.setMedicalRecordCode(src.getMedicalRecordCode());
        copy.setFullName(src.getFullName());
        copy.setDateOfBirth(src.getDateOfBirth());
        copy.setGender(src.getGender());
        copy.setPhone(src.getPhone());
        copy.setEmail(src.getEmail());
        copy.setAddress(src.getAddress());
        copy.setNotes(src.getNotes());
        copy.setLastTestDate(src.getLastTestDate());
        copy.setCreatedAt(src.getCreatedAt());
        copy.setCreatedBy(src.getCreatedBy());
        copy.setUpdatedAt(src.getUpdatedAt());
        copy.setUpdatedBy(src.getUpdatedBy());
        copy.setDeletedAt(src.getDeletedAt());
        return copy;
    }
}
