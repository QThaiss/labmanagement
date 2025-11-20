/*
 * @ {#} WorkflowResponse.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.dtos.response;

import fit.instrument_service.enums.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: DTO representing the response of a workflow status
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowResponse {
    private String workflowId;
    private String instrumentId;
    private String cassetteId;
    private WorkflowStatus status;
    private List<String> sampleIds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private boolean reagentCheckPassed;
    private boolean testOrderServiceAvailable;
    private String errorMessage;
}
