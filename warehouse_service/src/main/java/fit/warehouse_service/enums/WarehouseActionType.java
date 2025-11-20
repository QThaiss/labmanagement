/*
 * @ (#) WarehouseActionType.java    1.0    27/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.enums;/*
 * @description:
 * @author: Bao Thong
 * @date: 27/10/2025
 * @version: 1.0
 */

public enum WarehouseActionType {
    INSTRUMENT_CREATED,
    INSTRUMENT_STATUS_UPDATED,
    INSTRUMENT_DEACTIVATED,
    INSTRUMENT_ACTIVATED,
    INSTRUMENT_DELETED,
    CONFIG_CREATED,
    CONFIG_UPDATED,
    CONFIG_DELETED,
    REAGENT_RECEIVED,
    REAGENT_USED,
    INSTRUMENT_DELETION_SCHEDULED,
    INSTRUMENT_DELETION_CANCELLED,
}
