package fit.warehouse_service.mappers;

import fit.warehouse_service.dtos.response.ReagentUsageHistoryResponse;
import fit.warehouse_service.entities.ReagentUsageHistory;
import org.springframework.stereotype.Component;

@Component
public class ReagentUsageHistoryMapper {

    public ReagentUsageHistoryResponse toResponse(ReagentUsageHistory entity) {
        return ReagentUsageHistoryResponse.builder()
                .id(entity.getId())
                .timestamp(entity.getCreatedAt())
                .responsibleUserId(entity.getCreatedByUserId())
                .reagentName(entity.getReagentLot().getReagentType().getName())
                .reagentLotNumber(entity.getReagentLot().getLotNumber())
                .instrumentName(entity.getInstrument() != null ? entity.getInstrument().getName() : "N/A")
                .quantityUsed(entity.getQuantityUsed())
                .action(entity.getAction())
                .reagentTypeId(entity.getReagentLot().getReagentType().getId())
                .reagentLotId(entity.getReagentLot().getId())
                .instrumentId(entity.getInstrument() != null ? entity.getInstrument().getId() : null)
                .build();
    }
}