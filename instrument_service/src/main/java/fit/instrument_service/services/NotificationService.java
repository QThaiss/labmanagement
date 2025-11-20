/*
 * @ {#} NotificationService.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services;

import fit.instrument_service.entities.BloodSample;

/*
 * @description: Service gửi thông báo liên quan đến mẫu máu và thiết bị
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public interface NotificationService {
    /**
     *  Gửi thông báo cập nhật trạng thái mẫu máu
     *
     * @param sample Mẫu máu cần cập nhật trạng thái
     */
    void notifySampleStatusUpdate(BloodSample sample);

    /**
     * Gửi thông báo hoàn thành quy trình xử lý mẫu máu
     *
     * @param workflowId  ID của quy trình xử lý
     * @param instrumentId ID của thiết bị
     */
    void notifyWorkflowCompletion(String workflowId, String instrumentId);

    /**
     * Gửi thông báo khi thiết bị thiếu hóa chất
     *
     * @param instrumentId ID của thiết bị
     */
    void notifyInsufficientReagents(String instrumentId);
}
