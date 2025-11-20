/*
 * @ {#} TestCatalogResponse.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCatalogResponse {
    private Long id;

    private String loincCode;

    private String localCode;

    private String testName;

    private String specimenType;

    private String unit;

    private String referenceRange;

    private String method;

    private Boolean active;

}
