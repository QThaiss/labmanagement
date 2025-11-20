package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.ReagentUsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReagentUsageHistoryRepository extends JpaRepository<ReagentUsageHistory, String>,
        JpaSpecificationExecutor<ReagentUsageHistory> {
}