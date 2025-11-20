package fit.warehouse_service.controllers;

import fit.warehouse_service.dtos.request.CreateVendorRequest;
import fit.warehouse_service.dtos.request.UpdateVendorRequest;
import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.dtos.response.VendorResponse;
import fit.warehouse_service.services.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/warehouse/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'LAB_USER')")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorById(@PathVariable String id) {

        VendorResponse responseDto = vendorService.getVendorById(id);

        ApiResponse<VendorResponse> apiResponse = ApiResponse.<VendorResponse>builder()
                .success(true)
                .message("Vendor details retrieved successfully.")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<VendorResponse>> createVendor(@Valid @RequestBody CreateVendorRequest request) {
        VendorResponse responseDto = vendorService.createVendor(request);

        ApiResponse<VendorResponse> apiResponse = ApiResponse.<VendorResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Vendor created successfully.")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendor(
            @PathVariable String id,
            @Valid @RequestBody UpdateVendorRequest request) {

        VendorResponse responseDto = vendorService.updateVendor(id, request);

        ApiResponse<VendorResponse> apiResponse = ApiResponse.<VendorResponse>builder()
                .success(true)
                .message("Vendor updated successfully.")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable String id) {
        vendorService.deleteVendor(id);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .message("Vendor deleted successfully.")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'LAB_USER')")
    public ResponseEntity<ApiResponse<PageResponse<VendorResponse>>> getAllVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String[] sort,
            @RequestParam(required = false) String search) {

        PageResponse<VendorResponse> response = vendorService.getAllVendors(page, size, sort, search);

        ApiResponse<PageResponse<VendorResponse>> apiResponse = ApiResponse.<PageResponse<VendorResponse>>builder()
                .success(true)
                .message("Vendors retrieved successfully.")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}