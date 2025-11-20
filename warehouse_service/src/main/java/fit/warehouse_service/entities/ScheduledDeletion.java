/*
 * @ {#} ScheduledDeletion.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.entities;

import fit.warehouse_service.utils.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * @description: Entity representing a scheduled deletion of an instrument.
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
@Entity
@Table(name = "scheduled_deletions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledDeletion extends BaseEntity {
    @Column(name = "instrument_id", nullable = false)
    private String instrumentId;

    @Column(name = "scheduled_deletion_time", nullable = false)
    private LocalDateTime scheduledDeletionTime;

    @Column(name = "deactivation_time", nullable = false)
    private LocalDateTime deactivationTime;

    @Column(name = "is_cancelled", nullable = false)
    private boolean cancelled = false;

    @Column(name = "cancellation_time")
    private LocalDateTime cancellationTime;

    @Column(name = "reason")
    private String reason;

    @Override
    public String generateId() {
        return IdGenerator.generate("SHD");
    }
}
