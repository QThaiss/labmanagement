/*
 * @ {#} RoleConstants.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.constants;

import lombok.experimental.UtilityClass;

/*
 * @description: Constants for user roles
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@UtilityClass
public class RoleConstants {
    public static final String ADMIN = "ADMIN";

    public static final String LAB_USER = "LAB_USER";

    public static final String LAB_MANAGER = "LAB_MANAGER";

    public static final String HAS_ROLE_LAB_USER = "hasRole('LAB_USER')";

    public static final String CAN_CRUD_PMR = "hasAnyRole('" + ADMIN + "', '" + LAB_USER + "')";

    public static final String CAN_READ_PMR = "hasAnyRole('" + ADMIN + "', '" + LAB_USER + "', '" + LAB_MANAGER + "')";
}
