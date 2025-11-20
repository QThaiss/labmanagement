package fit.warehouse_service.mappers;

import fit.warehouse_service.dtos.request.CreateVendorRequest;
import fit.warehouse_service.dtos.request.UpdateVendorRequest;
import fit.warehouse_service.dtos.response.VendorResponse;
import fit.warehouse_service.entities.Vendor;
import org.springframework.stereotype.Component;

@Component
public class VendorMapper {

    public VendorResponse toResponse(Vendor vendor) {
        if (vendor == null) {
            return null;
        }
        VendorResponse dto = new VendorResponse();
        dto.setId(vendor.getId());
        dto.setName(vendor.getName());
        dto.setContactPerson(vendor.getContactPerson());
        dto.setEmail(vendor.getEmail());
        dto.setPhone(vendor.getPhone());
        dto.setAddress(vendor.getAddress());
        return dto;
    }

    public Vendor toEntity(CreateVendorRequest request) {
        if (request == null) return null;
        Vendor vendor = new Vendor();
        vendor.setName(request.getName());
        vendor.setContactPerson(request.getContactPerson());
        vendor.setEmail(request.getEmail());
        vendor.setPhone(request.getPhone());
        vendor.setAddress(request.getAddress());
        return vendor;
    }

    public void updateEntityFromRequest(Vendor vendor, UpdateVendorRequest request) {
        if (request.getName() != null) vendor.setName(request.getName());
        if (request.getContactPerson() != null) vendor.setContactPerson(request.getContactPerson());
        if (request.getEmail() != null) vendor.setEmail(request.getEmail());
        if (request.getPhone() != null) vendor.setPhone(request.getPhone());
        if (request.getAddress() != null) vendor.setAddress(request.getAddress());
    }
}