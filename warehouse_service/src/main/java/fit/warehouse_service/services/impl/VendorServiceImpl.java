package fit.warehouse_service.services.impl;

import fit.warehouse_service.dtos.request.CreateVendorRequest;
import fit.warehouse_service.dtos.request.UpdateVendorRequest;
import fit.warehouse_service.dtos.response.FilterInfo;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.dtos.response.VendorResponse;
import fit.warehouse_service.entities.Vendor;
import fit.warehouse_service.exceptions.AlreadyExistsException;
import fit.warehouse_service.exceptions.NotFoundException;
import fit.warehouse_service.mappers.VendorMapper;
import fit.warehouse_service.repositories.VendorRepository;
import fit.warehouse_service.services.VendorService;
import fit.warehouse_service.specifications.VendorSpecification;
import fit.warehouse_service.utils.SecurityUtils;
import fit.warehouse_service.utils.SortUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;
    private final VendorSpecification vendorSpecification;

    // Định nghĩa các trường cho phép sort
    private static final Set<String> VENDOR_SORT_FIELDS = Set.of("id", "name", "createdAt", "updatedAt", "email");
    private static final String DEFAULT_SORT = "createdAt";

    @Override
    public VendorResponse getVendorById(String vendorId) {
        log.info("Fetching vendor by ID: {}", vendorId);
        Vendor vendor = vendorRepository.findById(vendorId)
                .filter(v -> !v.isDeleted()) // Kiểm tra chưa bị xóa mềm
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + vendorId));

        return vendorMapper.toResponse(vendor);
    }

    @Override
    @Transactional
    public VendorResponse createVendor(CreateVendorRequest request) {
        log.info("Creating new vendor: {}", request.getName());

        if (vendorRepository.existsByName(request.getName())) {
            throw new AlreadyExistsException("Vendor with name '" + request.getName() + "' already exists");
        }
        if (request.getEmail() != null && vendorRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Vendor with email '" + request.getEmail() + "' already exists");
        }

        Vendor vendor = vendorMapper.toEntity(request);

        // AuditingListener sẽ tự động điền createdAt, createdBy nếu có cấu hình.
        // Tuy nhiên, có thể set thủ công nếu cần thiết hoặc BaseEntity yêu cầu.
        vendor.setCreatedByUserId(SecurityUtils.getCurrentUserId());
        vendor.setCreatedAt(LocalDateTime.now());

        Vendor savedVendor = vendorRepository.save(vendor);
        return vendorMapper.toResponse(savedVendor);
    }

    @Override
    @Transactional
    public VendorResponse updateVendor(String id, UpdateVendorRequest request) {
        log.info("Updating vendor with ID: {}", id);

        Vendor vendor = vendorRepository.findById(id)
                .filter(v -> !v.isDeleted())
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + id));

        // Validate unique name if changing
        if (request.getName() != null && !request.getName().equals(vendor.getName())) {
            if (vendorRepository.existsByName(request.getName())) {
                throw new AlreadyExistsException("Vendor with name '" + request.getName() + "' already exists");
            }
        }

        // Validate unique email if changing
        if (request.getEmail() != null && !request.getEmail().equals(vendor.getEmail())) {
            if (vendorRepository.existsByEmail(request.getEmail())) {
                throw new AlreadyExistsException("Vendor with email '" + request.getEmail() + "' already exists");
            }
        }

        vendorMapper.updateEntityFromRequest(vendor, request);

        // Cập nhật audit fields
        vendor.setUpdatedByUserId(SecurityUtils.getCurrentUserId());
        vendor.setUpdatedAt(LocalDateTime.now());

        Vendor updatedVendor = vendorRepository.save(vendor);
        return vendorMapper.toResponse(updatedVendor);
    }

    @Override
    @Transactional
    public void deleteVendor(String id) {
        log.info("Deleting vendor with ID: {}", id);

        Vendor vendor = vendorRepository.findById(id)
                .filter(v -> !v.isDeleted())
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + id));

        // Soft Delete
        vendor.setDeleted(true);
        vendor.setDeletedAt(LocalDateTime.now());
        vendor.setUpdatedByUserId(SecurityUtils.getCurrentUserId());

        vendorRepository.save(vendor);
        log.info("Vendor deleted successfully (soft delete): {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VendorResponse> getAllVendors(int page, int size, String[] sort, String search) {
        log.info("Fetching all vendors with search: {}", search);

        Sort validSort = SortUtils.buildSort(sort, VENDOR_SORT_FIELDS, DEFAULT_SORT);
        Pageable pageable = PageRequest.of(page, size, validSort);

        Specification<Vendor> spec = vendorSpecification.build(search);

        Page<Vendor> vendorPage = vendorRepository.findAll(spec, pageable);
        Page<VendorResponse> dtoPage = vendorPage.map(vendorMapper::toResponse);

        FilterInfo filterInfo = FilterInfo.builder()
                .search(search)
                .build();

        return PageResponse.from(dtoPage, filterInfo);
    }
}