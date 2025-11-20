package fit.warehouse_service.dtos.response;

import lombok.Data;

@Data
public class VendorResponse {
    private String id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
}