/*
 * @ (#) ReportJob.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.JobStatus;
import fit.test_order_service.enums.JobType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report_job", indexes = {
        @Index(name = "idx_job_type_status", columnList = "job_type, status"),
        @Index(name = "idx_job_requester_time", columnList = "requested_by, created_at DESC"),
        @Index(name = "idx_job_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportJob {
    @Id
    @Column(name = "job_id", length = 36, nullable = false, updatable = false)
    private String jobId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 24, nullable = false)
    private JobType jobType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private JobStatus status;

    @Min(0)
    @Max(100)
    @Column(name = "progress_pct", columnDefinition = "tinyint unsigned")
    private Integer progressPct;

    @NotBlank
    @Column(name = "requested_by", length = 36, nullable = false)
    private String requestedBy;

    @Column(name = "order_id", length = 36)
    private String orderId; // for PRINT

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params_json", columnDefinition = "json")
    private String paramsJson;

    @Column(name = "result_file_id", length = 36)
    private String resultFileId;

    @Size(max = 512)
    @Column(name = "message", length = 512)
    private String message;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at", columnDefinition = "datetime(6)")
    private LocalDateTime startedAt;

    @Column(name = "finished_at", columnDefinition = "datetime(6)")
    private LocalDateTime finishedAt;

    /* Relations */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_file_id", referencedColumnName = "file_id",
            foreignKey = @ForeignKey(name = "fk_job_result_file"), insertable = false, updatable = false)
    private ReportFileStore resultFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id",
            foreignKey = @ForeignKey(name = "fk_job_order"), insertable = false, updatable = false)
    private TestOrder printOrderRef;

    @OneToMany(mappedBy = "jobRef", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<ReportJobLog> logs;

    @OneToMany(mappedBy = "jobRef", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<ReportNotification> notifications;

    @OneToMany(mappedBy = "jobRef", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<PrintSnapshot> snapshots;

    @PrePersist
    void pp() {
        if (jobId == null) jobId = UUID.randomUUID().toString();
        if (status == null) status = JobStatus.QUEUED;
        if (progressPct == null) progressPct = 0;
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
