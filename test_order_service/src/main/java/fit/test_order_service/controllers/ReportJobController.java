/*
 * @ (#) ReportJobController.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.controllers;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.dtos.response.FileStoreResponse;
import fit.test_order_service.dtos.response.JobStatusResponse;
import fit.test_order_service.entities.ReportFileStore;
import fit.test_order_service.entities.ReportJob;
import fit.test_order_service.exceptions.NotFoundException;
import fit.test_order_service.repositories.ReportJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/report-jobs")
@RequiredArgsConstructor
public class ReportJobController {

    private final ReportJobRepository reportJobRepository;

    @GetMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<JobStatusResponse>> getJobStatus(@PathVariable String jobId) {

        ReportJob job = reportJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Not found jobId: " + jobId));

        FileStoreResponse fileResponse = null;
        // Kiểm tra xem job đã có file kết quả chưa
        if (job.getResultFile() != null) {
            ReportFileStore fileEntity = job.getResultFile();

            // Map Entity (fileEntity) sang DTO (fileResponse)
            fileResponse = FileStoreResponse.builder()
                    .fileId(fileEntity.getFileId())
                    .storageType(fileEntity.getStorageType())
                    .objectKey(fileEntity.getObjectKey())
                    .fileName(fileEntity.getFileName())
                    .mimeType(fileEntity.getMimeType())
                    .byteSize(fileEntity.getByteSize())
                    .createdBy(fileEntity.getCreatedBy())
                    .createdAt(fileEntity.getCreatedAt())
                    .build();
        }

        // Map Entity (job) sang DTO (jobResponse)
        JobStatusResponse jobResponse = JobStatusResponse.builder()
                .jobId(job.getJobId())
                .status(job.getStatus())
                .message(job.getMessage())
                .requestedAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .progressPct(job.getProgressPct())
                .resultFile(fileResponse)
                .build();

        return ResponseEntity.ok(ApiResponse.success(jobResponse, "Job status retrieved successfully"));
    }
}
