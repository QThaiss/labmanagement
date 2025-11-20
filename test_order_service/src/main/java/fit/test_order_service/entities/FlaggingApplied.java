/*
 * @ (#) FlaggingApplied.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.FlagSeverity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "flagging_applied", indexes = {
        @Index(name = "idx_flagapplied_result", columnList = "result_id"),
        @Index(name = "idx_flagapplied_code", columnList = "flag_code, severity"),
        @Index(name = "idx_flagapplied_rulever", columnList = "rule_version")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlaggingApplied {
    @Id
    @Column(name = "applied_id", length = 36, nullable = false, updatable = false)
    private String appliedId;

    @NotBlank
    @Column(name = "result_id", length = 36, nullable = false)
    private String resultId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "rule_id", length = 64, nullable = false)
    private String ruleId;

    @NotNull
    @Column(name = "rule_version", nullable = false)
    private Integer ruleVersion;

    @NotBlank
    @Size(max = 32)
    @Column(name = "flag_code", length = 32, nullable = false)
    private String flagCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 16, nullable = false)
    private FlagSeverity severity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_json", columnDefinition = "json")
    private String contextJson;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime createdAt;

    /* Relation */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", referencedColumnName = "result_id",
            foreignKey = @ForeignKey(name = "fk_flag_result"), insertable = false, updatable = false)
    private TestResult resultRef;

    @PrePersist
    void pp() {
        if (appliedId == null) appliedId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
