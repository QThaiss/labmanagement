/*
 * @ {#} MedicalRecordControllerTest.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.patient_service.dtos.response.MedicalRecordResponse;
import fit.patient_service.enums.RecordStatus;
import fit.patient_service.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
//@WebMvcTest(
//        controllers = MedicalController.class,
//        excludeAutoConfiguration = {
//                DataSourceAutoConfiguration.class,
//                HibernateJpaAutoConfiguration.class,
//                JpaRepositoriesAutoConfiguration.class
//        }
//)
class MedicalRecordControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private MedicalRecordService medicalRecordService;
//
//    @MockitoBean
//    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    @DisplayName("DELETE /api/v1/medical-records/{medicalRecordId} -> 200 OK")
//    void softDeleteMedicalRecord_ShouldReturnDeletedMedicalRecord_WhenValidMedicalRecordId() throws Exception {
//        String medicalRecordId = "medical123";
//        MedicalRecordResponse deletedMedicalRecordResponse = MedicalRecordResponse.builder()
//                .medicalRecordId(medicalRecordId)
//                .visitTime(LocalDateTime.now().minusDays(1))
//                .lastTestTime(LocalDateTime.now().minusDays(1))
//                .status(RecordStatus.OPEN)
//                .doctorId("doctor123")
//                .deletedAt(LocalDateTime.now())
//                .build();
//
//        when(medicalRecordService.softDeleteMedicalRecord(medicalRecordId)).thenReturn(deletedMedicalRecordResponse);
//
//        mockMvc.perform(delete("/api/v1/medical-records/{medicalRecordId}", medicalRecordId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Medical record soft deleted successfully"))
//                .andExpect(jsonPath("$.data.medicalRecordId").value(medicalRecordId))
//                .andExpect(jsonPath("$.data.deletedAt").exists());
//
//        verify(medicalRecordService, times(1)).softDeleteMedicalRecord(medicalRecordId);
//    }
//
//    @Test
//    @DisplayName("DELETE /api/v1/medical-records/{medicalRecordId} -> 404 Not Found")
//    void softDeleteMedicalRecord_ShouldReturnNotFound_WhenMedicalRecordNotExists() throws Exception {
//        String medicalRecordId = "nonexistent";
//
//        when(medicalRecordService.softDeleteMedicalRecord(medicalRecordId))
//                .thenThrow(new NotFoundException(NotFoundException.MEDICAL_RECORD_NOT_FOUND + medicalRecordId));
//
//        mockMvc.perform(delete("/api/v1/medical-records/{medicalRecordId}", medicalRecordId))
//                .andExpect(status().isNotFound());
//
//        verify(medicalRecordService, times(1)).softDeleteMedicalRecord(medicalRecordId);
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/medical-records/{medicalRecordId} -> 200 OK")
//    void getMedicalRecordById_ShouldReturnMedicalRecord_WhenValidId() throws Exception {
//        String medicalRecordId = "medical123";
//        MedicalRecordResponse medicalRecordResponse = MedicalRecordResponse.builder()
//                .medicalRecordId(medicalRecordId)
//                .patientId("patient123")
//                .visitTime(LocalDateTime.now().minusDays(1))
//                .lastTestTime(LocalDateTime.now())
//                .status(RecordStatus.OPEN)
//                .doctorId("doctor123")
//                .notes("Patient checkup completed")
//                .createdAt(LocalDateTime.now().minusDays(2))
//                .createdBy("admin")
//                .build();
//
//        when(medicalRecordService.getMedicalRecordById(medicalRecordId))
//                .thenReturn(medicalRecordResponse);
//
//        mockMvc.perform(get("/api/v1/medical-records/{medicalRecordId}", medicalRecordId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.medicalRecordId").value(medicalRecordId))
//                .andExpect(jsonPath("$.data.patientId").value("patient123"))
//                .andExpect(jsonPath("$.data.status").value("OPEN"))
//                .andExpect(jsonPath("$.data.doctorId").value("doctor123"))
//                .andExpect(jsonPath("$.data.notes").value("Patient checkup completed"));
//
//        verify(medicalRecordService, times(1)).getMedicalRecordById(medicalRecordId);
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/medical-records/{medicalRecordId} -> 404 Not Found")
//    void getMedicalRecordById_ShouldReturnNotFound_WhenMedicalRecordNotExists() throws Exception {
//        String medicalRecordId = "nonexistent";
//
//        when(medicalRecordService.getMedicalRecordById(medicalRecordId))
//                .thenThrow(new NotFoundException(NotFoundException.MEDICAL_RECORD_NOT_FOUND + medicalRecordId));
//
//        mockMvc.perform(get("/api/v1/medical-records/{medicalRecordId}", medicalRecordId))
//                .andExpect(status().isNotFound());
//
//        verify(medicalRecordService, times(1)).getMedicalRecordById(medicalRecordId);
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/medical-records/{medicalRecordId} -> 404 Not Found (soft deleted)")
//    void getMedicalRecordById_ShouldReturnNotFound_WhenMedicalRecordSoftDeleted() throws Exception {
//        String medicalRecordId = "deleted123";
//
//        when(medicalRecordService.getMedicalRecordById(medicalRecordId))
//                .thenThrow(new NotFoundException(NotFoundException.MEDICAL_RECORD_NOT_FOUND + medicalRecordId));
//
//        mockMvc.perform(get("/api/v1/medical-records/{medicalRecordId}", medicalRecordId))
//                .andExpect(status().isNotFound());
//
//        verify(medicalRecordService, times(1)).getMedicalRecordById(medicalRecordId);
//    }
}
