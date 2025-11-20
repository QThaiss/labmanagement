package fit.instrument_service.dtos.response;

import lombok.Data;

@Data
public class VendorResponse {
    // Phải khớp với DTO của warehouse
    private String id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
}