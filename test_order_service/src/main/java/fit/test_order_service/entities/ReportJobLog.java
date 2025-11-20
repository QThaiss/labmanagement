/*
 * @ (#) ReportJobLog.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.LogLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "report_job_log", indexes = {
        @Index(name = "idx_joblog_job_time", columnList = "job_id, created_at DESC"),
        @Index(name = "idx_joblog_level", columnList = "level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportJobLog {
    @Id
    @Column(name = "log_id", length = 36, nullable = false, updatable = false)
    private String logId;

    @NotBlank
    @Column(name = "job_id", length = 36, nullable = false)
    private String jobId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 8, nullable = false)
    private LogLevel level;

    @NotBlank
    @Size(max = 64)
    @Column(name = "event", length = 64, nullable = false)
    private String event;

    @Size(max = 512)
    @Column(name = "message", length = 512)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_json", columnDefinition = "json")
    private String metaJson;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime createdAt;

    /* Relation */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", referencedColumnName = "job_id",
            foreignKey = @ForeignKey(name = "fk_joblog_job"), insertable = false, updatable = false)
    private ReportJob jobRef;

    @PrePersist
    void pp() {
        if (logId == null) logId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
