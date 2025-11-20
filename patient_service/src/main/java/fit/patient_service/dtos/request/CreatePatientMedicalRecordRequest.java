/*
 * @ {#} CreatePatientMedicalRecordRequest.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.dtos.request;

import jakarta.validation.constraints.*;


/*
 * @description: DTO for creating a new patient medical record
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
public record CreatePatientMedicalRecordRequest (
    @NotBlank(message = "Full name is required")
    @Size(min = 1, max = 150, message = "Full name must be between 1 and 150 characters")
    String fullName,

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in format yyyy-MM-dd")
    String dateOfBirth,

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    String gender,

    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must be 10 digits and start with 0")
    String phone,

    @Email(message = "Invalid email format")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    String email,

    @Size(max = 4096, message = "Address must not exceed 4096 characters")
    String address,

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes
) {}
