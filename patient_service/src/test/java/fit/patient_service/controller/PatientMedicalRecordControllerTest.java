package fit.patient_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.patient_service.config.TestSecurityConfig;
import fit.patient_service.controllers.PatientMedicalRecordController;
import fit.patient_service.dtos.request.CreatePatientMedicalRecordRequest;
import fit.patient_service.dtos.request.UpdatePatientMedicalRecordRequest;
import fit.patient_service.dtos.response.PatientMedicalRecordResponse;
import fit.patient_service.enums.Gender;
import fit.patient_service.exceptions.NotFoundException;
import fit.patient_service.services.PatientMedicalRecordService;
import fit.patient_service.security.GatewayOnlyFilter; // Lớp Filter cần loại trừ

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Vẫn cần để giả lập trạng thái đăng nhập
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Loại trừ GatewayOnlyFilter để các request MockMvc không bị chặn bởi lỗi
 * "Forbidden: Must go through gateway".
 */
@WebMvcTest(
        controllers = PatientMedicalRecordController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GatewayOnlyFilter.class
        )
)
@Import(TestSecurityConfig.class)
// Giả lập một người dùng đã đăng nhập đơn giản cho tất cả các test (bỏ qua kiểm tra quyền)
@WithMockUser(username = "testuser")
class PatientMedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientMedicalRecordService patientMedicalRecordService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE_URL = "/api/v1/patient-medical-records";
    private PatientMedicalRecordResponse mockResponse;
    private CreatePatientMedicalRecordRequest validCreateRequest;

    private final LocalDateTime NOW = LocalDateTime.of(2025, 11, 25, 10, 0, 0);

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreatePatientMedicalRecordRequest(
                "Le Van An",
                "1990-10-10",
                "MALE",
                "0987654321",
                "le.van.an@example.com",
                "123 Ho Chi Minh",
                "Initial checkup"
        );

        mockResponse = new PatientMedicalRecordResponse(
                "MR123456",
                "MRC-123",
                "Le Van An",
                LocalDateTime.of(1990, 10, 10, 0, 0),
                Gender.MALE,
                "0987654321",
                "le.van.an@example.com",
                "123 Ho Chi Minh",
                "Initial checkup",
                NOW.minusDays(1),
                NOW.minusDays(2),
                "BS001",
                NOW.minusDays(2),
                "BS001",
                null
        );
    }

    // --- Test POST /api/v1/patient-medical-records ---
    @Test
    @DisplayName("POST / -> 201 Created (Success)")
    void createPatientMedicalRecord_ShouldReturn201_WhenValidRequest() throws Exception {
        when(patientMedicalRecordService.createPatientMedicalRecord(any(CreatePatientMedicalRecordRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Patient medical record created successfully"))
                .andExpect(jsonPath("$.data.medicalRecordId").value("MR123456"))
                .andExpect(jsonPath("$.data.fullName").value("Le Van An"));

        verify(patientMedicalRecordService, times(1)).createPatientMedicalRecord(any(CreatePatientMedicalRecordRequest.class));
    }

    @Test
    @DisplayName("POST / -> 400 Bad Request (Validation Error: Missing Full Name)")
    void createPatientMedicalRecord_ShouldReturn400_WhenMissingRequiredField() throws Exception {
        CreatePatientMedicalRecordRequest invalidRequest = new CreatePatientMedicalRecordRequest(
                "", // Blank full name
                "1990-10-10",
                "MALE",
                "0987654321",
                "le.van.an@example.com",
                "123 Ho Chi Minh",
                "Initial checkup"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(patientMedicalRecordService, never()).createPatientMedicalRecord(any());
    }

    // --- Test PUT /api/v1/patient-medical-records/{medicalRecordCode} ---
    @Test
    @DisplayName("PUT /{medicalRecordCode} -> 200 OK (Success)")
    void updatePatientMedicalRecord_ShouldReturn200_WhenValidRequest() throws Exception {
        String medicalRecordCode = "MRC-123";

        UpdatePatientMedicalRecordRequest validUpdateRequest = new UpdatePatientMedicalRecordRequest(
                "Le Van New Name",
                null,
                "FEMALE",
                null,
                null,
                null,
                "Updated Notes"
        );

        PatientMedicalRecordResponse updatedResponse = new PatientMedicalRecordResponse(
                mockResponse.medicalRecordId(),
                mockResponse.medicalRecordCode(),
                "Le Van New Name",
                mockResponse.dateOfBirth(),
                Gender.FEMALE,
                mockResponse.phone(),
                mockResponse.email(),
                mockResponse.address(),
                "Updated Notes",
                mockResponse.lastTestDate(),
                mockResponse.createdAt(),
                mockResponse.createdBy(),
                NOW.plusHours(1),
                "LAB_USER",
                mockResponse.deletedAt()
        );

        when(patientMedicalRecordService.updatePatientMedicalRecord(eq(medicalRecordCode), any(UpdatePatientMedicalRecordRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put(BASE_URL + "/{medicalRecordCode}", medicalRecordCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Patient medical record updated successfully"))
                .andExpect(jsonPath("$.data.fullName").value("Le Van New Name"))
                .andExpect(jsonPath("$.data.gender").value("FEMALE"));

        verify(patientMedicalRecordService, times(1)).updatePatientMedicalRecord(eq(medicalRecordCode), any(UpdatePatientMedicalRecordRequest.class));
    }

    @Test
    @DisplayName("PUT /{medicalRecordCode} -> 404 Not Found")
    void updatePatientMedicalRecord_ShouldReturn404_WhenRecordNotFound() throws Exception {
        String medicalRecordCode = "NON_EXISTENT";
        UpdatePatientMedicalRecordRequest validUpdateRequest = new UpdatePatientMedicalRecordRequest(
                "Le Van New Name", null, null, null, null, null, null
        );

        // Mock service ném ra NotFoundException
        when(patientMedicalRecordService.updatePatientMedicalRecord(eq(medicalRecordCode), any(UpdatePatientMedicalRecordRequest.class)))
                .thenThrow(new NotFoundException(NotFoundException.PATIENT_MEDICAL_RECORD_NOT_FOUND + medicalRecordCode));

        mockMvc.perform(put(BASE_URL + "/{medicalRecordCode}", medicalRecordCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(NotFoundException.PATIENT_MEDICAL_RECORD_NOT_FOUND + medicalRecordCode));

        verify(patientMedicalRecordService, times(1)).updatePatientMedicalRecord(eq(medicalRecordCode), any(UpdatePatientMedicalRecordRequest.class));
    }

    // --- Test GET /api/v1/patient-medical-records/code/{medicalRecordCode} ---
    @Test
    @DisplayName("GET /code/{medicalRecordCode} -> 200 OK")
    void getPatientMedicalRecordByCode_ShouldReturn200_WhenRecordExists() throws Exception {
        String medicalRecordCode = "MRC-123";

        when(patientMedicalRecordService.getPatientMedicalRecordByCode(medicalRecordCode))
                .thenReturn(mockResponse);

        mockMvc.perform(get(BASE_URL + "/code/{medicalRecordCode}", medicalRecordCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.medicalRecordCode").value(medicalRecordCode));

        verify(patientMedicalRecordService, times(1)).getPatientMedicalRecordByCode(medicalRecordCode);
    }
}