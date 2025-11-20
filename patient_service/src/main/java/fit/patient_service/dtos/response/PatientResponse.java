/*
 * @ {#} PatientResponse.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.dtos.response;

import fit.patient_service.enums.Gender;
import lombok.*;

import java.time.LocalDateTime;

/*
 * @description: Standard API response wrapper
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {
    private String patientId;

    private String patientCode;

    private String fullName;

    private LocalDateTime dateOfBirth;

    private Gender gender;

    private String phone;

    private String email;

    private String address;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private String createdBy;

    private String updatedBy;

}
