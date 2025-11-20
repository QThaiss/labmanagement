/*
 * @ (#) AccessLog.java    1.0    23/09/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.patient_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 23/09/2025
 * @version: 1.0
 */

import fit.patient_service.enums.AccessAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs",
        indexes = {
                @Index(name = "idx_log_record", columnList = "medical_record_id"),
                @Index(name = "idx_log_time", columnList = "access_time")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @Column(length = 100, name = "access_log_id")
    private String accessLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_medical_record_id")
    private PatientMedicalRecord patientMedicalRecord;

    @Column(name = "user_id", nullable = false)
    private String userId;            // từ IAM

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 16, nullable = false)
    private AccessAction action;

    @Column(name = "changed_fields_json", columnDefinition = "TEXT")
    private String changedFieldsJson;  // khi UPDATE/DELETE: lưu diff ngắn gọn

    @Column(name = "access_time", nullable = false)
    private LocalDateTime accessTime;
}
