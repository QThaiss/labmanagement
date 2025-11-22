/*
 * @ (#) TestResult.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.AbnormalFlag;
import fit.test_order_service.enums.EntrySource;
import fit.test_order_service.enums.FlagSeverity;
import fit.test_order_service.utils.TestResultGenerator;
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
@Table(name = "test_result", indexes = {
        @Index(name = "idx_result_order", columnList = "order_id"),
        @Index(name = "idx_result_analyte", columnList = "analyte_name"),
        @Index(name = "idx_result_test_code", columnList = "test_code"),
//        @Index(name = "idx_result_flag", columnList = "flag_code, flag_severity"),
//        @Index(name = "idx_result_rule_ver", columnList = "applied_rule_version")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {
    @Id
    @Column(name = "result_id", length = 36, nullable = false, updatable = false)
    private String resultId;

    @NotBlank
    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;

    @Column(name = "test_code", length = 36)
    private String testCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_source", length = 12, nullable = false)
    private EntrySource entrySource; // HL7, MANUAL, IMPORT

    @Column(name = "entered_by", length = 36)
    private String enteredBy;

    @Column(name = "entered_at", columnDefinition = "datetime(6)")
    private LocalDateTime enteredAt;

    @NotBlank
    @Size(max = 128)
    @Column(name = "analyte_name", length = 128, nullable = false)
    private String analyteName;

    @NotBlank
    @Size(max = 64)
    @Column(name = "value_text", length = 64, nullable = false)
    private String valueText;

    @Size(max = 32)
    @Column(name = "unit", length = 32)
    private String unit;

    @Size(max = 64)
    @Column(name = "reference_range", length = 64)
    private String referenceRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "abnormal_flag", length = 8)
    private AbnormalFlag abnormalFlag;

    @Column(name = "measured_at", columnDefinition = "datetime(6)")
    private LocalDateTime measuredAt;

    @Size(max = 64)
    @Column(name = "source_msg_id", length = 64)
    private String sourceMsgId;

//    @Column(name = "applied_rule_version")
//    private Integer appliedRuleVersion;

//    @Size(max = 32)
//    @Column(name = "flag_code", length = 32)
//    private String flagCode;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "flag_severity", length = 16)
//    private FlagSeverity flagSeverity;

//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "meta_json", columnDefinition = "json")
//    private String metaJson;

    @Column(name = "created_at", columnDefinition = "datetime(6)")
    private LocalDateTime createdAt;

    /* Relations */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id",
            foreignKey = @ForeignKey(name = "fk_result_order"), insertable = false, updatable = false)
    private TestOrder orderRef;

    @OneToMany(mappedBy = "resultRef", fetch = FetchType.LAZY)
    private List<FlaggingApplied> flags;

//    @OneToMany(mappedBy = "resultRef", fetch = FetchType.LAZY)
//    private List<OrderComment> comments;

    @OneToMany(mappedBy = "resultRef", fetch = FetchType.LAZY)
    private List<TestResultAdjustLog> adjustLogs;

    @PrePersist
    void pp() {
        if (resultId == null) resultId = TestResultGenerator.generateTestResultId();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);

        // Mặc định khi nhập thủ công qua REST
        if (entrySource == null) entrySource = EntrySource.MANUAL;
        if (enteredAt == null) enteredAt = LocalDateTime.now(ZoneOffset.UTC);

        // Fallback đo lường (không bắt buộc, có thể để service gán theo order.runAt)
        if (measuredAt == null) measuredAt = createdAt;
    }
}
