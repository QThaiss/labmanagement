/*
 * @ {#} InstrumentOrderRequest.java   1.0     13/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.dtos.request;

import fit.test_order_service.dtos.response.TestOrderDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   13/11/2025
 * @version:    1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentOrderRequest implements Serializable {
    // Gửi toàn bộ chi tiết order
    private TestOrderDetailResponse orderDetails;
}
