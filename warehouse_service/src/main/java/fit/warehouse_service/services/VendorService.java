package fit.warehouse_service.services;

import fit.warehouse_service.dtos.request.CreateVendorRequest;
import fit.warehouse_service.dtos.request.UpdateVendorRequest;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.dtos.response.VendorResponse;

public interface VendorService {
    VendorResponse getVendorById(String vendorId);

    VendorResponse createVendor(CreateVendorRequest request);

    VendorResponse updateVendor(String id, UpdateVendorRequest request);

    void deleteVendor(String id);

    PageResponse<VendorResponse> getAllVendors(int page, int size, String[] sort, String search);
}