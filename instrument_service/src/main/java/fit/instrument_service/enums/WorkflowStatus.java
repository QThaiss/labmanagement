/*
 * @ {#} WorkflowStatus.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.enums;

/*
 * @description: Trạng thái của quy trình xử lý mẫu trong thiết bị phân tích
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public enum WorkflowStatus {
    INITIATED,          // Quy trình đã được khởi tạo
    VALIDATING,         // Đang xác thực mẫu
    RUNNING,            // Quy trình đang chạy
    COMPLETED,          // Quy trình hoàn thành thành công
    FAILED,             // Quy trình thất bại
    HALTED              // Quy trình làm việc bị dừng do thiếu thuốc thử hoặc lỗi
}
