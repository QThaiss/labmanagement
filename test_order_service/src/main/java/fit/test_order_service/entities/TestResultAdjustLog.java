/*
 * @ (#) TestResultAdjustLog.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.ReviewMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "test_result_adjust_log", indexes = {
        @Index(name = "idx_adj_order_time", columnList = "order_id, created_at DESC"),
        @Index(name = "idx_adj_result_time", columnList = "result_id, created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultAdjustLog {
    @Id
    @Column(name = "adjust_id", length = 36, nullable = false, updatable = false)
    private String adjustId;

    @NotBlank
    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;

    @NotBlank
    @Column(name = "result_id", length = 36, nullable = false)
    private String resultId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "review_mode", length = 8, nullable = false)
    private ReviewMode reviewMode;

    @Column(name = "actor_user_id", length = 36)
    private String actorUserId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "field", length = 64, nullable = false)
    private String field;

    @Size(max = 128)
    @Column(name = "before_value", length = 128)
    private String beforeValue;

    @NotBlank
    @Size(max = 128)
    @Column(name = "after_value", length = 128, nullable = false)
    private String afterValue;

    @Size(max = 255)
    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime createdAt;

    /* Relations */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id",
            foreignKey = @ForeignKey(name = "fk_adj_order"), insertable = false, updatable = false)
    private TestOrder orderRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", referencedColumnName = "result_id",
            foreignKey = @ForeignKey(name = "fk_adj_result"), insertable = false, updatable = false)
    private TestResult resultRef;

    @PrePersist
    void pp() {
        if (adjustId == null) adjustId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}