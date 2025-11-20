/*
 * @ {#} ReagentUsageLimit.java   1.0     20/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * @description: DTO đại diện cho giới hạn sử dụng của một loại hóa chất
 * @author: Tran Hien Vinh
 * @date:   20/11/2025
 * @version:    1.0
 */
@Data
@AllArgsConstructor
public class ReagentUsageLimit {
    private double min;
    private double max;
}
