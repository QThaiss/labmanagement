package fit.warehouse_service.services;

import fit.warehouse_service.dtos.request.LogReagentUsageRequest;
import fit.warehouse_service.dtos.request.ReceiveReagentRequest;
import fit.warehouse_service.dtos.response.ReagentLotStatusResponse;
import fit.warehouse_service.dtos.response.ReagentSupplyHistoryResponse;
import fit.warehouse_service.dtos.response.ReagentUsageHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;


public interface ReagentHistoryService {


    ReagentSupplyHistoryResponse receiveReagentShipment(ReceiveReagentRequest request);

    Page<ReagentSupplyHistoryResponse> getReagentSupplyHistory(
            String reagentTypeId, String vendorId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    ReagentSupplyHistoryResponse getReagentSupplyHistoryById(String historyId);

    ReagentUsageHistoryResponse logReagentUsage(LogReagentUsageRequest request);

    Page<ReagentUsageHistoryResponse> getReagentUsageHistory(String reagentTypeId, String reagentLotId, Pageable pageable);

    boolean checkReagentStockExists(String vendorId, String lotNumber);

    ReagentLotStatusResponse getReagentLotStatus(String lotNumber);
}