package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.ReagentLot;
import fit.warehouse_service.entities.ReagentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReagentLotRepository extends JpaRepository<ReagentLot, String> {
    ReagentLot findByLotNumber(String lotNumber);

    List<ReagentLot> findByReagentTypeAndExpirationDateGreaterThanEqualAndCurrentQuantityGreaterThanOrderByExpirationDateAsc(ReagentType reagentType, LocalDate minExpirationDate, double minQuantity
    );

    @Query("SELECT r FROM ReagentLot r " +
            "WHERE r.reagentType.name = :reagentName " +
            "AND r.currentQuantity > 0 " +
            "AND r.expirationDate >= :currentDate " +
            "ORDER BY r.expirationDate ASC")
    List<ReagentLot> findAvailableLotsByName(
            @Param("reagentName") String reagentName,
            @Param("currentDate") LocalDate currentDate);
}