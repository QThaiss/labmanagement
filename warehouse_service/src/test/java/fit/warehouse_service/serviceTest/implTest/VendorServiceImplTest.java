package fit.warehouse_service.serviceTest.implTest;

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
import fit.warehouse_service.services.impl.VendorServiceImpl;
import fit.warehouse_service.specifications.VendorSpecification;
import fit.warehouse_service.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorServiceImplTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private VendorMapper vendorMapper;

    @Mock
    private VendorSpecification vendorSpecification;

    @InjectMocks
    private VendorServiceImpl vendorService;

    @Test
    void getVendorById_returnsResponse() {
        Vendor vendor = new Vendor();
        vendor.setId("V1");
        vendor.setName("ABC");

        VendorResponse response = new VendorResponse();
        response.setId("V1");

        when(vendorRepository.findById("V1")).thenReturn(Optional.of(vendor));
        when(vendorMapper.toResponse(vendor)).thenReturn(response);

        VendorResponse result = vendorService.getVendorById("V1");

        assertEquals("V1", result.getId());
    }

    @Test
    void getVendorById_notFound_throwsException() {
        when(vendorRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> vendorService.getVendorById("missing"));
    }

    @Test
    void getVendorById_deletedVendor_throwsException() {
        Vendor vendor = new Vendor();
        vendor.setId("V1");
        vendor.setDeleted(true);

        when(vendorRepository.findById("V1")).thenReturn(Optional.of(vendor));

        assertThrows(NotFoundException.class, () -> vendorService.getVendorById("V1"));
    }

    @Test
    void createVendor_success() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setName("Vendor A");
        request.setEmail("vendor@example.com");

        Vendor vendor = new Vendor();
        vendor.setName("Vendor A");
        vendor.setEmail("vendor@example.com");
        vendor.setCreatedAt(LocalDateTime.now());

        VendorResponse response = new VendorResponse();
        response.setId("V1");

        when(vendorRepository.existsByName("Vendor A")).thenReturn(false);
        when(vendorRepository.existsByEmail("vendor@example.com")).thenReturn(false);
        when(vendorMapper.toEntity(request)).thenReturn(vendor);
        when(vendorRepository.save(vendor)).thenReturn(vendor);
        when(vendorMapper.toResponse(vendor)).thenReturn(response);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            VendorResponse result = vendorService.createVendor(request);

            assertEquals("V1", result.getId());
            verify(vendorRepository).save(vendor);
        }
    }

    @Test
    void createVendor_duplicateName_throwsException() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setName("Vendor A");

        when(vendorRepository.existsByName("Vendor A")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> vendorService.createVendor(request));
    }

    @Test
    void createVendor_duplicateEmail_throwsException() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setName("Vendor A");
        request.setEmail("vendor@example.com");

        when(vendorRepository.existsByName("Vendor A")).thenReturn(false);
        when(vendorRepository.existsByEmail("vendor@example.com")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> vendorService.createVendor(request));
    }

    @Test
    void updateVendor_success() {
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setName("Vendor B");
        request.setEmail("new@example.com");

        Vendor vendor = new Vendor();
        vendor.setId("V1");
        vendor.setName("Vendor A");
        vendor.setEmail("old@example.com");

        when(vendorRepository.findById("V1")).thenReturn(Optional.of(vendor));
        when(vendorRepository.existsByName("Vendor B")).thenReturn(false);
        when(vendorRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(vendorRepository.save(vendor)).thenReturn(vendor);
        when(vendorMapper.toResponse(vendor)).thenReturn(new VendorResponse());

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            vendorService.updateVendor("V1", request);

            assertEquals("Vendor B", vendor.getName());
            assertEquals("new@example.com", vendor.getEmail());
        }
    }

    @Test
    void updateVendor_notFound_throwsException() {
        when(vendorRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> vendorService.updateVendor("missing", new UpdateVendorRequest()));
    }

    @Test
    void updateVendor_duplicateName_throwsException() {
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setName("Vendor B");

        Vendor vendor = new Vendor();
        vendor.setId("V1");
        vendor.setName("Vendor A");

        when(vendorRepository.findById("V1")).thenReturn(Optional.of(vendor));
        when(vendorRepository.existsByName("Vendor B")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> vendorService.updateVendor("V1", request));
    }

    @Test
    void updateVendor_duplicateEmail_throwsException() {
        UpdateVendorRequest request = new UpdateVendorRequest();
        request.setEmail("vendor@example.com");

        Vendor vendor = new Vendor();
        vendor.setId("V1");
        vendor.setEmail("old@example.com");

        when(vendorRepository.findById("V1")).thenReturn(Optional.of(vendor));
        when(vendorRepository.existsByEmail("vendor@example.com")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> vendorService.updateVendor("V1", request));
    }

    @Test
    void deleteVendor_marksDeleted() {
        Vendor vendor = new Vendor();
        vendor.setId("V1");

        when(vendorRepository.findById("V1")).thenReturn(Optional.of(vendor));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            vendorService.deleteVendor("V1");

            assertTrue(vendor.isDeleted());
            verify(vendorRepository).save(vendor);
        }
    }

//    @Test
//    void getAllVendors_appliesSortingAndSpecification() {
//        Vendor vendor = new Vendor();
//        vendor.setId("V1");
//        VendorResponse vendorResponse = new VendorResponse();
//        vendorResponse.setId("V1");
//
//        Specification<Vendor> specification = mock(Specification.class);
//        when(vendorSpecification.build("search"))
//                .thenReturn(specification);
//
//        Page<Vendor> vendorPage = new PageImpl<>(List.of(vendor), PageRequest.of(0, 10), 1);
//        when(vendorRepository.findAll(any(Specification.class), any(Pageable.class)))
//                .thenReturn(vendorPage);
//        when(vendorMapper.toResponse(vendor)).thenReturn(vendorResponse);
//
//        PageResponse<VendorResponse> response = vendorService.getAllVendors(0, 10, new String[]{"name,desc"}, "search");
//
//        assertEquals(1, response.getItems().size());
//        assertEquals("V1", response.getItems().getFirst().getId());
//        assertEquals(FilterInfo.builder().search("search").build().getSearch(), response.getFilterInfo().getSearch());
//        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("name")));
//        verify(vendorRepository).findAll(eq(specification), eq(pageable));
//    }
}