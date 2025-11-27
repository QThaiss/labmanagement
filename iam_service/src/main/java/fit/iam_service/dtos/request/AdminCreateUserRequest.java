/*
 * @ {#} AdminCreateUserRequest.java   1.0     25/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.dtos.request;

import jakarta.validation.constraints.*;

/*
 * @description: DTO for admin to create a new user.
 * @author: Tran Hien Vinh
 * @date:   25/11/2025
 * @version:    1.0
 */
public record AdminCreateUserRequest(
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
        @Pattern(regexp = "^ROLE_[A-Z0-9_]+$", message = "Role code must start with ROLE_")
        @Size(max = 50)
        String roleCode
) {}
