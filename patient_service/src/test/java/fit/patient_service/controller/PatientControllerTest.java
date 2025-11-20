/*
 * @ {#} PatientControllerTest.java   1.0     25/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.controller;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   25/09/2025
 * @version:    1.0
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.patient_service.dtos.response.PatientResponse;
import fit.patient_service.enums.Gender;
import fit.patient_service.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest(
//        controllers = PatientController.class,
//        excludeAutoConfiguration = {
//                DataSourceAutoConfiguration.class,
//                HibernateJpaAutoConfiguration.class,
//                JpaRepositoriesAutoConfiguration.class
//        }
//)
class PatientControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private PatientService patientService;
//
//    @MockitoBean
//    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private CreatePatientRequest validRequest;
//    private PatientResponse patientResponse;
//
//    @BeforeEach
//    void setUp() {
//        validRequest = CreatePatientRequest.builder()
//                .fullName("Phan Minh Hai")
//                .phone("0357954627")
//                .email("phamminhhai@gmail.com")
//                .dateOfBirth("1990-01-01")
//                .gender("MALE")
//                .address("4 Nguyen Van Viet Boulevard, District 1, Ho Chi Minh City, Vietnam")
//                .build();
//
//        patientResponse = PatientResponse.builder()
//                .fullName("Phan Minh Hai")
//                .phone("0357954627")
//                .email("phamminhhai@gmail.com")
//                .dateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0))
//                .gender(Gender.MALE)
//                .address("4 Nguyen Van Viet Boulevard, District 1, Ho Chi Minh City, Vietnam")
//                .createdBy("admin")
//                .build();
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/patients -> 201 Created")
//    void createPatient_ShouldReturnCreatedPatient_WhenValidRequest() throws Exception {
//        when(patientService.createPatient(any(CreatePatientRequest.class))).thenReturn(patientResponse);
//
//        mockMvc.perform(post("/api/v1/patients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Patient created successfully"))
//                .andExpect(jsonPath("$.data.fullName").value("Phan Minh Hai"))
//                .andExpect(jsonPath("$.data.phone").value("0357954627"))
//                .andExpect(jsonPath("$.data.email").value("phamminhhai@gmail.com"));
//
//        ArgumentCaptor<CreatePatientRequest> captor = ArgumentCaptor.forClass(CreatePatientRequest.class);
//        verify(patientService, times(1)).createPatient(captor.capture());
//        assertEquals("Phan Minh Hai", captor.getValue().getFullName());
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/patients -> 400 Bad Request (validation)")
//    void createPatient_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
//        // Missing required fullName and invalid email to trigger Bean Validation errors
//        CreatePatientRequest invalid = CreatePatientRequest.builder()
//                .phone("0357954627")
//                .email("phamminhhai@gmail.com")
//                .dateOfBirth("1990-01-01")
//                .gender("MALE")
//                .address("4 Nguyen Van Viet Boulevard, District 1, Ho Chi Minh City, Vietnam")
//                .build();
//
//        mockMvc.perform(post("/api/v1/patients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalid)))
//                .andExpect(status().isBadRequest());
//
//        verify(patientService, never()).createPatient(any());
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/patients -> 409 Conflict (duplicate email/phone)")
//    void createPatient_ShouldReturnConflict_WhenDuplicateEmailOrPhone() throws Exception {
//        CreatePatientRequest duplicate = CreatePatientRequest.builder()
//                .fullName("John Doe")
//                .phone("0123456789")
//                .email("dup@example.com")
//                .dateOfBirth("1990-01-01")
//                .gender("MALE")
//                .address("123 Main St")
//                .build();
//
//        when(patientService.createPatient(any(CreatePatientRequest.class)))
//                .thenThrow(new org.springframework.web.server.ResponseStatusException(
//                        org.springframework.http.HttpStatus.CONFLICT, "Email or phone already exists"));
//
//        mockMvc.perform(post("/api/v1/patients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(duplicate)))
//                .andExpect(status().isConflict());
//
//        org.mockito.ArgumentCaptor<CreatePatientRequest> captor =
//                org.mockito.ArgumentCaptor.forClass(CreatePatientRequest.class);
//        verify(patientService, times(1)).createPatient(captor.capture());
//        Assertions.assertEquals("dup@example.com", captor.getValue().getEmail());
//        Assertions.assertEquals("0123456789", captor.getValue().getPhone());
//    }
//
//    @Test
//    @DisplayName("DELETE /api/v1/patients/{patientId} -> 200 OK")
//    void softDeletePatient_ShouldReturnDeletedPatient_WhenValidPatientId() throws Exception {
//        String patientId = "patient123";
//        PatientResponse deletedPatientResponse = PatientResponse.builder()
//                .patientId(patientId)
//                .patientCode("P001")
//                .fullName("John Doe")
//                .phone("0123456789")
//                .email("john@example.com")
//                .deletedAt(LocalDateTime.now())
//                .build();
//
//        when(patientService.softDeletePatient(patientId)).thenReturn(deletedPatientResponse);
//
//        mockMvc.perform(delete("/api/v1/patients/{patientId}", patientId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Patient soft deleted successfully"))
//                .andExpect(jsonPath("$.data.patientId").value(patientId))
//                .andExpect(jsonPath("$.data.deletedAt").exists());
//
//        verify(patientService, times(1)).softDeletePatient(patientId);
//    }
//
//    @Test
//    @DisplayName("DELETE /api/v1/patients/{patientId} -> 404 Not Found")
//    void softDeletePatient_ShouldReturnNotFound_WhenPatientNotExists() throws Exception {
//        String patientId = "nonexistent";
//
//        when(patientService.softDeletePatient(patientId))
//                .thenThrow(new NotFoundException(NotFoundException.PATIENT_NOT_FOUND + patientId));
//
//        mockMvc.perform(delete("/api/v1/patients/{patientId}", patientId))
//                .andExpect(status().isNotFound());
//
//        verify(patientService, times(1)).softDeletePatient(patientId);
//    }
}
