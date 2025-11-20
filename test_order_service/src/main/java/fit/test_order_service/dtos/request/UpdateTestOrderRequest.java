/*
 * @ {#} UpdateTestOrderRequest.java   1.0     13/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/*
 * @description: Request DTO for updating a test order
 * @author: Tran Hien Vinh
 * @date:   13/10/2025
 * @version:    1.0
 */
@Data
public class UpdateTestOrderRequest {
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$",
            message = "Gender must be one of: MALE, FEMALE, OTHER")
    private String gender;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Phone number contains invalid characters")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    @Email(message = "Email must be in valid format")
    @Size(max = 128, message = "Email must not exceed 128 characters")
    private String email;
}
