/*
 * @ {#} SampleStatus.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.enums;

/*
 * @description: Trạng thái của mẫu máu trong quy trình xử lý
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public enum SampleStatus {
    PENDING,        // Mẫu chờ xử lý
    VALIDATED,      // Mẫu đã được xác thực
    QUEUED,         // Mẫu đã được xếp vào hàng đợi chờ phân tích
    PROCESSING,     // Mẫu đang được phân tích
    COMPLETED,      // Mẫu đã hoàn thành phân tích
    SKIPPED,        // Mẫu bị bỏ qua
    FAILED          // Mẫu phân tích thất bại
}
