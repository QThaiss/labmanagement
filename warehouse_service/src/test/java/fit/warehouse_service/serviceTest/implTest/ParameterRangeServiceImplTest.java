package fit.warehouse_service.serviceTest.implTest;

import fit.warehouse_service.dtos.request.CreateParameterRangeRequest;
import fit.warehouse_service.dtos.request.UpdateParameterRangeRequest;
import fit.warehouse_service.dtos.response.ParameterRangeResponse;
import fit.warehouse_service.entities.ParameterRange;
import fit.warehouse_service.entities.TestParameter;
import fit.warehouse_service.exceptions.AlreadyExistsException;
import fit.warehouse_service.exceptions.NotFoundException;
import fit.warehouse_service.mappers.ParameterRangeMapper;
import fit.warehouse_service.repositories.ParameterRangeRepository;
import fit.warehouse_service.repositories.TestParameterRepository;
import fit.warehouse_service.services.impl.ParameterRangeServiceImpl;
import fit.warehouse_service.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParameterRangeServiceImplTest {

    @Mock
    private ParameterRangeRepository parameterRangeRepository;

    @Mock
    private TestParameterRepository testParameterRepository;

    @Mock
    private ParameterRangeMapper parameterRangeMapper;

    @InjectMocks
    private ParameterRangeServiceImpl service;

    private TestParameter testParameter;

    @BeforeEach
    void setUp() {
        testParameter = new TestParameter();
        testParameter.setId("TP-1");
        testParameter.setParamName("Glucose");
        testParameter.setAbbreviation("glu");
    }

    @Test
    void createParameterRange_success() {
        CreateParameterRangeRequest request = new CreateParameterRangeRequest();
        request.setAbbreviation("GLU");
        request.setGender("MALE");
        request.setMinValue(1.0);
        request.setMaxValue(5.0);
        request.setUnit("mg/dL");

        ParameterRange parameterRange = new ParameterRange();
        parameterRange.setId("PR-1");
        parameterRange.setTestParameter(testParameter);
        parameterRange.setCreatedAt(LocalDateTime.now());

        ParameterRangeResponse response = ParameterRangeResponse.builder()
                .parameterRangeId("PR-1")
                .build();

        when(testParameterRepository.findByAbbreviation("GLU"))
                .thenReturn(Optional.of(testParameter));
        when(parameterRangeRepository.existsByTestParameterIdAndGender("TP-1", "MALE"))
                .thenReturn(false);
        when(parameterRangeRepository.save(any(ParameterRange.class))).thenReturn(parameterRange);
        when(parameterRangeMapper.mapToResponse(parameterRange)).thenReturn(response);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ParameterRangeResponse result = service.createParameterRange(request);

            assertEquals("PR-1", result.getParameterRangeId());
            verify(parameterRangeRepository).save(any(ParameterRange.class));
        }
    }

    @Test
    void createParameterRange_invalidRange_throwsException() {
        CreateParameterRangeRequest request = new CreateParameterRangeRequest();
        request.setAbbreviation("GLU");
        request.setGender("MALE");
        request.setMinValue(6.0);
        request.setMaxValue(5.0);

        assertThrows(IllegalArgumentException.class, () -> service.createParameterRange(request));
        verifyNoInteractions(testParameterRepository, parameterRangeRepository);
    }

    @Test
    void createParameterRange_missingTestParameter_throwsNotFound() {
        CreateParameterRangeRequest request = new CreateParameterRangeRequest();
        request.setAbbreviation("GLU");
        request.setGender("MALE");
        request.setMinValue(1.0);
        request.setMaxValue(5.0);

        when(testParameterRepository.findByAbbreviation("GLU"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createParameterRange(request));
    }

    @Test
    void createParameterRange_duplicateGender_throwsException() {
        CreateParameterRangeRequest request = new CreateParameterRangeRequest();
        request.setAbbreviation("GLU");
        request.setGender("MALE");
        request.setMinValue(1.0);
        request.setMaxValue(5.0);

        when(testParameterRepository.findByAbbreviation("GLU"))
                .thenReturn(Optional.of(testParameter));
        when(parameterRangeRepository.existsByTestParameterIdAndGender("TP-1", "MALE"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.createParameterRange(request));
        verify(parameterRangeRepository, never()).save(any());
    }

    @Test
    void updateParameterRange_successfullyUpdatesFields() {
        UpdateParameterRangeRequest request = new UpdateParameterRangeRequest();
        request.setGender("FEMALE");
        request.setMinValue(2.0);
        request.setMaxValue(6.0);
        request.setUnit("mg/dL");

        ParameterRange existing = new ParameterRange();
        existing.setId("PR-1");
        existing.setGender("MALE");
        existing.setMinValue(1.0);
        existing.setMaxValue(5.0);
        existing.setUnit("g/L");
        existing.setTestParameter(testParameter);

        when(parameterRangeRepository.findById("PR-1")).thenReturn(Optional.of(existing));
        when(parameterRangeRepository.existsByTestParameterIdAndGender("TP-1", "FEMALE"))
                .thenReturn(false);
        when(parameterRangeRepository.save(existing)).thenReturn(existing);
        when(parameterRangeMapper.mapToResponse(existing))
                .thenReturn(ParameterRangeResponse.builder().parameterRangeId("PR-1").gender("FEMALE").build());

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ParameterRangeResponse response = service.updateParameterRange("PR-1", request);

            assertEquals("FEMALE", response.getGender());
            assertEquals(2.0, existing.getMinValue());
            assertEquals(6.0, existing.getMaxValue());
            assertEquals("mg/dL", existing.getUnit());
        }
    }

    @Test
    void updateParameterRange_invalidMinMax_throwsException() {
        UpdateParameterRangeRequest request = new UpdateParameterRangeRequest();
        request.setMinValue(6.0);
        request.setMaxValue(5.0);

        ParameterRange existing = new ParameterRange();
        existing.setId("PR-1");
        existing.setGender("MALE");
        existing.setMinValue(1.0);
        existing.setMaxValue(5.0);
        existing.setTestParameter(testParameter);

        when(parameterRangeRepository.findById("PR-1")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.updateParameterRange("PR-1", request));
    }

    @Test
    void updateParameterRange_duplicateGender_throwsException() {
        UpdateParameterRangeRequest request = new UpdateParameterRangeRequest();
        request.setGender("FEMALE");

        ParameterRange existing = new ParameterRange();
        existing.setId("PR-1");
        existing.setGender("MALE");
        existing.setMinValue(1.0);
        existing.setMaxValue(5.0);
        existing.setTestParameter(testParameter);

        when(parameterRangeRepository.findById("PR-1")).thenReturn(Optional.of(existing));
        when(parameterRangeRepository.existsByTestParameterIdAndGender("TP-1", "FEMALE"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.updateParameterRange("PR-1", request));
        verify(parameterRangeRepository, never()).save(any());
    }

    @Test
    void deleteParameterRange_marksEntityDeleted() {
        ParameterRange existing = new ParameterRange();
        existing.setId("PR-1");
        existing.setGender("MALE");
        existing.setMinValue(1.0);
        existing.setMaxValue(5.0);
        existing.setTestParameter(testParameter);

        when(parameterRangeRepository.findById("PR-1")).thenReturn(Optional.of(existing));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            service.deleteParameterRange("PR-1");

            assertTrue(existing.isDeleted());
            verify(parameterRangeRepository).save(existing);
        }
    }

    @Test
    void restoreParameterRange_notDeleted_throwsException() {
        ParameterRange existing = new ParameterRange();
        existing.setId("PR-1");
        existing.setGender("MALE");
        existing.setTestParameter(testParameter);
        existing.setDeleted(false);

        when(parameterRangeRepository.findByIdIncludingDeleted("PR-1"))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.restoreParameterRange("PR-1"));
    }

    @Test
    void restoreParameterRange_conflictingGender_throwsAlreadyExists() {
        ParameterRange existing = new ParameterRange();
        existing.setId("PR-1");
        existing.setGender("MALE");
        existing.setTestParameter(testParameter);
        existing.setDeleted(true);

        when(parameterRangeRepository.findByIdIncludingDeleted("PR-1"))
                .thenReturn(Optional.of(existing));
        when(parameterRangeRepository.existsByTestParameterIdAndGenderAndDeletedFalse("TP-1", "MALE"))
                .thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> service.restoreParameterRange("PR-1"));
    }

    @Test
    void restoreParameterRange_successfulRestore() {
        ParameterRange existing = new ParameterRange();
        existing.setId("PR-1");
        existing.setGender("MALE");
        existing.setTestParameter(testParameter);
        existing.setDeleted(true);

        when(parameterRangeRepository.findByIdIncludingDeleted("PR-1"))
                .thenReturn(Optional.of(existing));
        when(parameterRangeRepository.existsByTestParameterIdAndGenderAndDeletedFalse("TP-1", "MALE"))
                .thenReturn(false);
        when(parameterRangeRepository.save(existing)).thenReturn(existing);
        when(parameterRangeMapper.mapToResponse(existing))
                .thenReturn(ParameterRangeResponse.builder().parameterRangeId("PR-1").build());

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ParameterRangeResponse response = service.restoreParameterRange("PR-1");

            assertFalse(existing.isDeleted());
            assertEquals("PR-1", response.getParameterRangeId());
        }
    }

    @Test
    void getParameterRangeById_notFound_throwsException() {
        when(parameterRangeRepository.findById("PR-1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getParameterRangeById("PR-1"));
    }

    @Test
    void getParameterRangeById_returnsResponse() {
        ParameterRange existing = new ParameterRange();
        existing.setId("PR-1");
        existing.setTestParameter(testParameter);

        ParameterRangeResponse response = ParameterRangeResponse.builder()
                .parameterRangeId("PR-1")
                .build();

        when(parameterRangeRepository.findById("PR-1")).thenReturn(Optional.of(existing));
        when(parameterRangeMapper.mapToResponse(existing)).thenReturn(response);

        ParameterRangeResponse result = service.getParameterRangeById("PR-1");

        assertEquals("PR-1", result.getParameterRangeId());
    }
}