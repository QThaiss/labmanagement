/*
 * @ {#} TestOrderStatusService.java   1.0     23/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services;

/*
 * @description: Service interface for managing TestOrder status updates.
 * @author: Tran Hien Vinh
 * @date:   23/10/2025
 * @version:    1.0
 */
public interface TestOrderStatusService {
    /**
     * Cập nhật trạng thái TestOrder nếu cần thiết dựa trên các điều kiện nhất định.
     *
     * @param orderId Mã TestOrder cần cập nhật trạng thái
     */
    void updateOrderStatusIfNeeded(String orderId);
}
