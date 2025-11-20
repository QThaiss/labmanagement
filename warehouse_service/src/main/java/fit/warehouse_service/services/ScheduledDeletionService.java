/*
 * @ {#} ScheduledDeletionService.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services;

import fit.warehouse_service.entities.ScheduledDeletion;

/*
 * @description: Service interface for managing scheduled deletions of instruments.
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
public interface ScheduledDeletionService {
    /**
     * Lên lịch xóa bỏ thiết bị y tế sau một khoảng thời gian nhất định
     *
     * @param instrumentId ID của thiết bị y tế cần xóa
     * @param reason Lý do xóa thiết bị
     *
     * @return Thông tin về việc lên lịch xóa thiết bị
     */
    ScheduledDeletion scheduleInstrumentDeletion(String instrumentId, String reason);

    /**
     * Hủy bỏ việc lên lịch xóa thiết bị y tế
     *
     * @param instrumentId ID của thiết bị y tế cần hủy việc xóa
     */
    void cancelScheduledDeletion(String instrumentId);

    /**
     * Thực hiện các xóa bỏ đã được lên lịch
     */
    void executePendingDeletions();

    /**
     * Xóa bỏ thiết bị y tế vĩnh viễn khỏi hệ thống
     */
    void deleteInstrumentPermanently(String instrumentId);
}
