/*
 * @ {#} ReagentCheckService.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services;

/*
 * @description: Service kiểm tra hóa chất của thiết bị
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public interface ReagentCheckService {
    /**
     * Kiểm tra xem hóa chất của thiết bị có đủ để vận hành không
     *
     * @param instrumentId ID của thiết bị
     * @return true nếu hóa chất đủ, false nếu không đủ
     */
    boolean areReagentsSufficient(String instrumentId);

    void uninstallReagent(String instrumentId, String instrumentReagentId, String reason);
}
