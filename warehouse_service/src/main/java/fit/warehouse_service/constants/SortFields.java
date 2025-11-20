/*
 * @ {#} SortFields.java   1.0     27/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.constants;

import lombok.experimental.UtilityClass;

import java.util.Set;

/*
 * @description: Utility class defining valid sort fields
 * @author: Tran Hien Vinh
 * @date:   27/09/2025
 * @version:    1.0
 */
@UtilityClass
public class SortFields {
    public static final Set<String> CONFIGURATION_SORT_FIELDS  =
            Set.of( "createdAt");

    public static final String DEFAULT_CONFIGURATION_SORT  = "id";

    // --- ĐÃ THÊM ---
    public static final Set<String> INSTRUMENT_SORT_FIELDS =
            Set.of("id", "name", "ipAddress", "createdAt", "updatedAt");

    public static final String DEFAULT_INSTRUMENT_SORT = "updatedAt";
    // --- KẾT THÚC THÊM ---

    public static final Set<String> TEST_PARAMETER_SORT_FIELDS =
            Set.of("createdAt", "paramName", "abbreviation", "updatedAt");

    public static final String DEFAULT_TEST_PARAMETER_SORT = "createdAt";
}
