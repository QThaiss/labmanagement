/*
 * @ (#) PrintSnapshot.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "print_snapshot", indexes = {
        @Index(name = "idx_snapshot_order_time", columnList = "order_id, created_at DESC"),
        @Index(name = "idx_snapshot_job", columnList = "job_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintSnapshot {
    @Id
    @Column(name = "snapshot_id", length = 36, nullable = false, updatable = false)
    private String snapshotId;

    @NotBlank
    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;

    @NotBlank
    @Column(name = "job_id", length = 36, nullable = false)
    private String jobId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "order_header_json", columnDefinition = "json", nullable = false)
    private String orderHeaderJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "results_json", columnDefinition = "json", nullable = false)
    private String resultsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "comments_json", columnDefinition = "json", nullable = false)
    private String commentsJson;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime createdAt;

    /* Relations */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id",
            foreignKey = @ForeignKey(name = "fk_snapshot_order"), insertable = false, updatable = false)
    private TestOrder orderRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", referencedColumnName = "job_id",
            foreignKey = @ForeignKey(name = "fk_snapshot_job"), insertable = false, updatable = false)
    private ReportJob jobRef;

    @PrePersist
    void pp() {
        if (snapshotId == null) snapshotId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}