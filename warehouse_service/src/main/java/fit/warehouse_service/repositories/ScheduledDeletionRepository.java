/*
 * @ {#} ScheduledDeletionRepository.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.ScheduledDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for managing ScheduledDeletion entities.
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
@Repository
public interface ScheduledDeletionRepository extends JpaRepository<ScheduledDeletion, String> {
    /**
     * Tìm kiếm một ScheduledDeletion chưa bị hủy bỏ theo instrumentId.
     *
     * @param instrumentId ID của công cụ.
     *
     * @return Optional chứa ScheduledDeletion nếu tìm thấy, ngược lại là rỗng.
     */
    Optional<ScheduledDeletion> findByInstrumentIdAndCancelledFalse(String instrumentId);

    /**
     * Tìm tất cả các ScheduledDeletion đã đến thời gian thực hiện và chưa bị hủy bỏ.
     *
     * @param currentTime Thời gian hiện tại để so sánh với scheduledDeletionTime.
     * @return Danh sách các ScheduledDeletion sẵn sàng để thực hiện.
     */
    @Query("SELECT sd FROM ScheduledDeletion sd WHERE sd.scheduledDeletionTime <= :currentTime AND sd.cancelled = false")
    List<ScheduledDeletion> findDeletionsReadyForExecution(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Tìm tất cả các ScheduledDeletion theo instrumentId.
     *
     * @param instrumentId ID của công cụ.
     * @return Danh sách các ScheduledDeletion liên quan đến instrumentId.
     */
    List<ScheduledDeletion> findByInstrumentId(String instrumentId);
}
