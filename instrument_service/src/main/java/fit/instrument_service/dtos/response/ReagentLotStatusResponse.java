package fit.instrument_service.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReagentLotStatusResponse {
    private String reagentLotId;
    private double currentQuantity;
}