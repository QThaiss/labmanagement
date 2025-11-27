/*
 * @ {#} ConfigurationFilterInfo.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.response;

import lombok.Builder;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
/*
 * @description: DTO for filtering configuration settings
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */

@Getter
@Setter
@Builder
public class FilterInfo {
    private String search;
    private String configType;
    private LocalDate startDate;
    private LocalDate endDate;
}
