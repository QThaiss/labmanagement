/*
 * @ (#) RawResultDeletedEvent.java    1.0    29/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.events;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/11/2025
 * @version: 1.0
 */

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RawResultDeletedEvent {
    private String eventId;         // Unique Event ID
    private String performedBy;     // User thực hiện (SRS 3.6.1.5)
    private List<String> deletedBarcodes; // Danh sách barcode đã xóa (SRS 3.6.1.5)
    private LocalDateTime deletedAt; // Timestamp (SRS 3.6.1.5)
    private String details;         // Thông tin thêm
}
