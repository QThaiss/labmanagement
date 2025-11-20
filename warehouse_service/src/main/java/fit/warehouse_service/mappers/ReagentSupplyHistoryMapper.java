/*
 * @ (#) ReagentSupplyHistoryMapper.java    1.0    30/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.mappers;

import fit.warehouse_service.dtos.response.ReagentSupplyHistoryResponse;
import fit.warehouse_service.entities.ReagentSupplyHistory;
import fit.warehouse_service.entities.ReagentType;
import fit.warehouse_service.entities.Vendor;
import org.springframework.stereotype.Component;

@Component
public class ReagentSupplyHistoryMapper {

    public ReagentSupplyHistoryResponse toResponse(ReagentSupplyHistory entity) {
        ReagentType reagentType = entity.getReagentType();
        Vendor vendor = entity.getVendor();

        return ReagentSupplyHistoryResponse.builder()
                .id(entity.getId())
                .reagentTypeId(reagentType != null ? reagentType.getId() : null)
                .reagentName(reagentType != null ? reagentType.getName() : null)
                .reagentCatalogNumber(reagentType != null ? reagentType.getCatalogNumber() : null)
                .reagentManufacturer(reagentType != null ? reagentType.getManufacturer() : null)
                .reagentCasNumber(reagentType != null ? reagentType.getCasNumber() : null)
                .vendorName(vendor != null ? vendor.getName() : null)
                .vendorId(vendor != null ? vendor.getId() : null)
                .poNumber(entity.getPoNumber())
                .orderDate(entity.getOrderDate())
                .receiptDate(entity.getReceiptDate())
                .quantityReceived(entity.getQuantityReceived())
                .unitOfMeasure(entity.getUnitOfMeasure())
                .lotNumber(entity.getLotNumber())
                .expirationDate(entity.getExpirationDate())
                .receivedByUserId(entity.getReceivedByUserId())
                .loggedByUserId(entity.getCreatedByUserId())
                .loggedAt(entity.getCreatedAt())
                .initialStorageLocation(entity.getInitialStorageLocation())
                .status(entity.getStatus())
                .build();
    }
}