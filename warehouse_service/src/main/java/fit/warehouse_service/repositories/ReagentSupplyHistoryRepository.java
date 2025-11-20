package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.ReagentSupplyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for ReagentSupplyHistory entity.
 */
@Repository
public interface ReagentSupplyHistoryRepository extends JpaRepository<ReagentSupplyHistory, String>,
        JpaSpecificationExecutor<ReagentSupplyHistory> {

    boolean existsByVendorIdAndLotNumber(String vendorId, String lotNumber);


}