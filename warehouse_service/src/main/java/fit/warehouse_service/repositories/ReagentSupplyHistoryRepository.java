package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.ReagentSupplyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ReagentSupplyHistory entity.
 */
@Repository
public interface ReagentSupplyHistoryRepository extends JpaRepository<ReagentSupplyHistory, String>,
        JpaSpecificationExecutor<ReagentSupplyHistory> {

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM ReagentSupplyHistory h WHERE h.vendor.id = :vendorId AND h.lotNumber = :lotNumber")
    boolean existsByVendorIdAndLotNumber(@Param("vendorId") String vendorId, @Param("lotNumber") String lotNumber);


}