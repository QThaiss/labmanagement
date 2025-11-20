package fit.warehouse_service.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVendorRequest {
    @NotBlank(message = "Vendor name is required")
    @Size(max = 255, message = "Vendor name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Contact person must not exceed 255 characters")
    private String contactPerson;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    @Size(max = 512, message = "Address must not exceed 512 characters")
    private String address;
}