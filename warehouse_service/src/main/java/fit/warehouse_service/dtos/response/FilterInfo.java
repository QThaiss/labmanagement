/*
 * @ {#} ConfigurationFilterInfo.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fit.warehouse_service.enums.DataType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/*
 * @description: DTO for filtering configuration settings
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterInfo {
    private String search;

    private DataType dataType;

    private LocalDate startDate;

    private LocalDate endDate;
}
