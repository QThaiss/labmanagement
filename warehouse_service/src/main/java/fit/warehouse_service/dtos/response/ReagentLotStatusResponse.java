package fit.warehouse_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReagentLotStatusResponse {
    private String reagentLotId;    // ID của Lô (ví dụ: RLO-xxx)
    private double currentQuantity; // Số lượng tồn kho hiện tại
}