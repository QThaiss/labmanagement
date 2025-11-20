/*
 * @ (#) AuditAction.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.enums;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

public enum AuditAction {
    // Test Flow
    MANUAL_DELETE_RAW_RESULT,
    AUTO_DELETE_RAW_RESULT,

    // Reagent
    INSTALL_REAGENT,
    MODIFY_REAGENT,
    DELETE_REAGENT,

    // Config
    SYNC_UP_CONFIGURATION
}