/*
 * @ {#} FilterInfo.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fit.patient_service.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/*
 * @description: Filter information for querying patient medical records
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterInfo {
    private String search;

    private LocalDate startDate;

    private LocalDate endDate;

    private Gender gender;
}
