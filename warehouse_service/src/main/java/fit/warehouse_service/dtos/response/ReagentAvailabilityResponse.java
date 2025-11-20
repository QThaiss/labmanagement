package fit.warehouse_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReagentAvailabilityResponse {
    private boolean exists;          // Tên thuốc có tồn tại trong danh mục không
    private double currentStock;     // Tổng số lượng tồn kho hiện tại
    private boolean enoughStock;     // Có đủ số lượng yêu cầu không
    private String unit;             // Đơn vị đo
}