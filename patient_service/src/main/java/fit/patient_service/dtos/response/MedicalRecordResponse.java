package fit.patient_service.dtos.response;

import fit.patient_service.enums.RecordStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordResponse {

    private String medicalRecordId;

    private String patientId;

    private LocalDateTime visitTime;

    private RecordStatus status;

    private LocalDateTime lastTestTime;

    private String doctorId;

    private String notes;

    private PatientSummaryResponse patient;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private String createdBy;

    private String updatedBy;

}