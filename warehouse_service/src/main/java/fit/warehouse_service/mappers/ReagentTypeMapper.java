package fit.warehouse_service.mappers;

import fit.warehouse_service.dtos.response.ReagentTypeResponse;
import fit.warehouse_service.entities.ReagentType;
import org.springframework.stereotype.Component;

@Component
public class ReagentTypeMapper {

    public ReagentTypeResponse toResponse(ReagentType entity) {
        if (entity == null) {
            return null;
        }

        return ReagentTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .catalogNumber(entity.getCatalogNumber())
                .manufacturer(entity.getManufacturer())
                .casNumber(entity.getCasNumber())
                .description(entity.getDescription())
                .usagePerRun(entity.getUsagePerRun())
                .isDeleted(entity.isDeleted())
                .build();
    }
}