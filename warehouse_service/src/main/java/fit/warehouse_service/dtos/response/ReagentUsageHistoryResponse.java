package fit.warehouse_service.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO chi tiết cho một bản ghi lịch sử sử dụng hóa chất.
 * Sẽ được sử dụng cho cả xem chi tiết và xem danh sách (theo yêu cầu).
 */
@Getter
@Setter
@Builder
public class ReagentUsageHistoryResponse {
    private String id;
    private LocalDateTime timestamp; // createdAt
    private String responsibleUserId; // createdByUserId
    private String reagentName;
    private String reagentLotNumber;
    private String instrumentName;
    private double quantityUsed;
    private String action;
    private String reagentTypeId;
    private String reagentLotId;
    private String instrumentId;
}