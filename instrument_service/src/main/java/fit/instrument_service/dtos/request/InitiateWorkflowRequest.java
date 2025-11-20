/*
 * @ {#} InitiateWorkflowRequest.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 * @description: Request DTO for initiating a sample analysis workflow
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateWorkflowRequest {

    @NotBlank(message = "Instrument ID is required")
    private String instrumentId;

    private String cassetteId;

    @NotEmpty(message = "At least one sample is required")
    @Valid
    private List<SampleInput> samples;
}
