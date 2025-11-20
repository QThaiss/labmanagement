/*
 * @ {#} AccessLogServiceImpl.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.services.impl;

import fit.patient_service.entities.AccessLog;
import fit.patient_service.entities.PatientMedicalRecord;
import fit.patient_service.enums.AccessAction;
import fit.patient_service.repositories.AccessLogRepository;
import fit.patient_service.services.AccessLogService;
import fit.patient_service.utils.AccessLogGenerator;
import fit.patient_service.utils.JsonUtils;
import fit.patient_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;

/*
 * @description: Implementation of AccessLogService for logging access actions on Patient entities
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class AccessLogServiceImpl implements AccessLogService {
    private static final String ENTITY_PATIENT_MEDICAL_RECORD = "PMR";

    private final AccessLogRepository accessLogRepository;

    @Override
    public void logCreatePatientMedicalRecord(PatientMedicalRecord patientMedicalRecord) {
        if (patientMedicalRecord == null) return;
        Map<String, Object> p = basePayload(AccessAction.CREATE);
        p.put("patientMedicalRecordId", patientMedicalRecord.getMedicalRecordId());
        p.put("medicalRecordCode", patientMedicalRecord.getMedicalRecordCode());
        p.put("fullName", patientMedicalRecord.getFullName());
        p.put("dateOfBirth", formatValue(patientMedicalRecord.getDateOfBirth()));
        p.put("gender", formatValue(patientMedicalRecord.getGender()));
        p.put("phone", patientMedicalRecord.getPhone());
        p.put("email", patientMedicalRecord.getEmail());
        p.put("address", patientMedicalRecord.getAddress());
        p.put("notes", patientMedicalRecord.getNotes());
        p.put("createdAt", formatValue(patientMedicalRecord.getCreatedAt()));
        p.put("createdBy", patientMedicalRecord.getCreatedBy());

        saveAccessLog(AccessAction.CREATE, patientMedicalRecord.getMedicalRecordId(), patientMedicalRecord, p);
    }

    @Override
    public void logUpdatePatientMedicalRecord(PatientMedicalRecord before, PatientMedicalRecord after) {
        if (after == null) return;

        Map<String, Object> diff = new HashMap<>();
        if (before != null) {
            fieldDiff(diff, "fullName", before.getFullName(), after.getFullName());
            fieldDiff(diff, "dateOfBirth", before.getDateOfBirth(), after.getDateOfBirth());
            fieldDiff(diff, "gender", before.getGender(), after.getGender());
            fieldDiff(diff, "phone", before.getPhone(), after.getPhone());
            fieldDiff(diff, "email", before.getEmail(), after.getEmail());
            fieldDiff(diff, "address", before.getAddress(), after.getAddress());
            fieldDiff(diff, "notes", before.getNotes(), after.getNotes());
        }
        if (diff.isEmpty()) return;

        Map<String, Object> p = basePayload(AccessAction.UPDATE);
        p.put("medicalRecordCode", after.getMedicalRecordCode());
        p.putAll(diff);

        saveAccessLog(AccessAction.UPDATE, after.getMedicalRecordId(), after, p);
    }

    @Override
    public void logDeletePatientMedicalRecord(PatientMedicalRecord patientMedicalRecord) {
        if (patientMedicalRecord == null) return;

        Map<String, Object> p = basePayload(AccessAction.DELETE);
        p.put("medicalRecordCode", patientMedicalRecord.getMedicalRecordCode());
        p.put("deletedAt", formatValue(patientMedicalRecord.getDeletedAt()));

        saveAccessLog(AccessAction.DELETE, patientMedicalRecord.getMedicalRecordId(), patientMedicalRecord, p);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void logGetAllPatientMedicalRecords(int page, int size, long totalElements, List<String> sorts) {
        Map<String, Object> p = basePayload(AccessAction.VIEW);
        p.put("page", page);
        p.put("size", size);
        p.put("totalElements", totalElements);
        p.put("sorts", sorts != null ? sorts : List.of());

        saveAccessLog(AccessAction.VIEW, "ALL", null, p);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void logGetPatientMedicalRecordByCode(PatientMedicalRecord patientMedicalRecord) {
        if (patientMedicalRecord == null) return;

        Map<String, Object> p = basePayload(AccessAction.VIEW);
        p.put("medicalRecordCode", patientMedicalRecord.getMedicalRecordCode());

        saveAccessLog(AccessAction.VIEW, patientMedicalRecord.getMedicalRecordId(), patientMedicalRecord, p);
    }

    @Override
    public void logGetPatientMedicalRecordById(PatientMedicalRecord patientMedicalRecord) {
        if (patientMedicalRecord == null) return;

        Map<String, Object> p = basePayload(AccessAction.VIEW);
        p.put("medicalRecordId", patientMedicalRecord.getMedicalRecordId());

        saveAccessLog(AccessAction.VIEW, patientMedicalRecord.getMedicalRecordId(), patientMedicalRecord, p);
    }

    // Helpers
    private Map<String, Object> basePayload(AccessAction action) {
        Map<String, Object> m = new HashMap<>();
        m.put("action", action.name());
        m.put("timestamp", now().toString());
        m.put("userId", SecurityUtils.getCurrentUserId());
        return m;
    }

    private void saveAccessLog(AccessAction action, Object recordId, PatientMedicalRecord record, Map<String, Object> payload) {
        AccessLog log = AccessLog.builder()
                .accessLogId(AccessLogGenerator.generateAccessLogId(
                        ENTITY_PATIENT_MEDICAL_RECORD,
                        String.valueOf(recordId),
                        action))
                .patientMedicalRecord(record)
                .userId(SecurityUtils.getCurrentUserId())
                .action(action)
                .changedFieldsJson(JsonUtils.convertToJson(payload))
                .accessTime(now())
                .build();

        accessLogRepository.save(log);
    }

    private void fieldDiff(Map<String, Object> diff, String field, Object oldV, Object newV) {
        if (oldV == null && newV == null) return;
        if (oldV == null || newV == null || !oldV.equals(newV)) {
            Map<String, Object> change = new HashMap<>();
            change.put("old", formatValue(oldV));
            change.put("new", formatValue(newV));
            diff.put(field, change);
        }
    }

    private Object formatValue(Object v) {
        if (v instanceof LocalDateTime ldt) return ldt.toString();
        if (v instanceof Enum<?> e) return e.name();
        return v;
    }

}