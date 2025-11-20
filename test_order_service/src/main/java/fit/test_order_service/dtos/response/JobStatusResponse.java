/*
 * @ (#) JobStatusResponse.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobStatusResponse {
    private String jobId;
    private JobStatus status;
    private String message;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private int progressPct;
    private FileStoreResponse resultFile;
}