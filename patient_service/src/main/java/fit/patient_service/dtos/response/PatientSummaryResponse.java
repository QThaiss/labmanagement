/*
 * @ (#) PatientSummaryResponse.java    1.0    25/09/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.patient_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/09/2025
 * @version: 1.0
 */

import fit.patient_service.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientSummaryResponse {
    private String patientId;
    private String fullName;
    private LocalDateTime dateOfBirth;
    private Gender gender;
}
