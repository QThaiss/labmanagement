/*
 * @ (#) CreateUserRequest.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import jakarta.validation.constraints.*;

public record CreateUserRequest(
        // Optional username (your User entity supports it)
        @Size(max = 50)
        String username,

        @NotBlank @Email @Size(max = 255)
        String email,

        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number format")

        @Size(max = 20)
        String phone,

        @NotBlank
        @Size(max = 100)
        String fullName,

        @NotBlank
        @Pattern(regexp = "^\\d{8,20}$", message = "Invalid identify number")

        @Size(max = 50)
        String identifyNumber,

        @NotBlank
        @Pattern(regexp = "(?i)^(MALE|FEMALE)$", message = "Gender must be MALE or FEMALE")
        String gender,

        // Provided by UI, we will cross-check with dob
        @NotNull
        @Min(0)
        @Max(150)
        Integer age,

        @NotBlank
        @Size(max = 255)
        String address,

        @NotBlank(message = "Date of birth is required (MM/DD/YYYY)")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-2][0-9]|3[01])/\\d{4}$",
                message = "Date of birth must be in MM/DD/YYYY format")
        String dateOfBirth,

        @NotBlank
//        @Size(min = 8, max = 128)
//        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,128}$", message = "Password must have letters and digits, 8-128 chars")
        String password,

        // Optional: assign role by code (e.g., "ROLE_USER"). If null we use default.
        @Size(max = 50)
        String roleCode
) {
}
