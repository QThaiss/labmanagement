/*
 * @ {#} PatientMedicalRecordClient.java   1.0     15/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.client;

import fit.test_order_service.client.dtos.PatientMedicalRecordInternalResponse;
import fit.test_order_service.services.impl.FeignClientConfig;
import fit.test_order_service.dtos.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
 * @description: Feign client for interacting with Patient Medical Record Service
 * @author: Tran Hien Vinh
 * @date:   15/10/2025
 * @version:    1.0
 */
@FeignClient(name = "patient-service", path = "/api/v1/internal/patient-medical-records", configuration = FeignClientConfig.class)
public interface PatientMedicalRecordFeignClient {
    @GetMapping("/code/{medicalRecordCode}")
    ApiResponse<PatientMedicalRecordInternalResponse> getPatientMedicalRecordByCode(@PathVariable("medicalRecordCode") String medicalRecordCode);

    @GetMapping("/{medicalRecordId}")
    ApiResponse<PatientMedicalRecordInternalResponse> getPatientMedicalRecordById(@PathVariable("medicalRecordId") String medicalRecordId);
}
