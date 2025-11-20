/*
 * @ {#} SampleResponse.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.dtos.response;

import fit.instrument_service.enums.SampleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description: DTO representing a sample response after workflow initiation
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SampleResponse {
    private String sampleId;
    private String barcode;
    private String testOrderId;
    private String workflowId;
    private String instrumentId;
    private SampleStatus status;
    private boolean isTestOrderAutoCreated;
    private String skipReason;
}
