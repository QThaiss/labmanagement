/*
 * @ (#) OrderEventLogService.java    1.0    13/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 13/10/2025
 * @version: 1.0
 */

import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.enums.EventType;

public interface OrderEventLogService {
    /**
     * Ghi lại một sự kiện liên quan đến một test order.
     *
     * @param order      TestOrder liên quan.
     * @param eventType  Loại sự kiện (CREATED, VIEWED, etc.).
     * @param details    Mô tả chi tiết về sự kiện.
     */
    void logEvent(TestOrder order, EventType eventType, String details);

    /**
     * Ghi lại sự thay đổi trạng thái của một test order.
     *
     * @param before  Trạng thái trước khi thay đổi.
     * @param after   Trạng thái sau khi thay đổi.
     * @param type    Loại sự kiện (UPDATED, etc.).
     */
    void logOrderUpdate(TestOrder before, TestOrder after, EventType type);
}
