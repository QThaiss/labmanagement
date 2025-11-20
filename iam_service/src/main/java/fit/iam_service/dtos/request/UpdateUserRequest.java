package fit.iam_service.dtos.request;

import jakarta.validation.constraints.*;

public record UpdateUserRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 150)
        String fullName,

        // MM/dd/yyyy như yêu cầu UI -> parse thủ công trong service
        @NotBlank(message = "Date of birth is required (MM/DD/YYYY)")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-2][0-9]|3[01])/\\d{4}$",
                message = "Date of birth must be in MM/DD/YYYY format")
        String dateOfBirth,

        @NotNull(message = "Age is required")
        @Min(value = 0, message = "Age must be >= 0")
        @Max(value = 150, message = "Age must be <= 150")
        Integer age,

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "^(?i)(male|female)$",
                message = "Gender must be either male or female")
        String gender,

        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String address,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is not a valid format")
        @Size(max = 255)
        String email,

        // E.164 (đồng nhất với entity)
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Phone must be E.164 (e.g. +84901234567)")
        @Size(max = 32)
        String phone
) {
}
