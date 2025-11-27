/*
 * @ {#} PatientMedicalRecordResponse.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.client.dtos;

import fit.test_order_service.enums.Gender;

import java.time.LocalDateTime;

/*
 * @description: DTO for patient medical record response
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */


public record PatientMedicalRecordInternalResponse(
    String medicalRecordId,

    String medicalRecordCode,

    String fullName,

    LocalDateTime dateOfBirth,

    Gender gender,

    String phone,

    String email,

    String address,

    String notes,

    LocalDateTime lastTestDate,

    LocalDateTime createdAt,

    String createdBy,

    LocalDateTime updatedAt,

    String updatedBy,

    LocalDateTime deletedAt
){}
