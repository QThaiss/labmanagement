/*
 * @ {#} SortFields.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.utils;

import lombok.experimental.UtilityClass;

import java.util.Set;

/*
 * @description: Utility class defining valid sort fields for test orders
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@UtilityClass
public class SortFields {
    public static final Set<String> TEST_ORDER_SORT_FIELDS =
            Set.of("createdAt", "patientName", "status", "reviewStatus", "updatedAt");

    public static final String DEFAULT_TEST_ORDER_SORT = "createdAt";
}
