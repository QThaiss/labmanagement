/*
 * @ (#) TestOrder.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.*;
import fit.test_order_service.utils.TestOrderGenerator;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/* TestOrder */
@Entity
@Table(name = "test_order", indexes = {
        @Index(name = "idx_order_medical_time", columnList = "medicalRecordCode, created_at DESC"),
        @Index(name = "idx_order_status_time", columnList = "status, created_at DESC"),
        @Index(name = "idx_order_review_time", columnList = "review_status, created_at DESC"),
        @Index(name = "idx_order_code", columnList = "order_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOrder {
    @Id
    @Column(name = "order_id", length = 36, nullable = false, updatable = false)
    private String orderId;

    @Size(max = 32)
    @Column(name = "order_code", length = 32, unique = true)
    private String orderCode;

    // refs qua service khác
    @NotBlank
    @Column(name = "medical_record_id", length = 40)
    private String medicalRecordId;

    @NotBlank
    @Column(name = "medical_record_code", nullable = false, length = 50)
    private String medicalRecordCode;

    // snapshot
    @NotBlank
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @NotNull
    @Column(name = "date_of_birth", columnDefinition = "date", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "barcode", unique = true)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_source")
    private EntrySource entrySource;

    @NotNull
    @Min(0)
    @Max(127)
    @Column(name = "age_years_snapshot", columnDefinition = "tinyint unsigned", nullable = false)
    private Integer ageYearsSnapshot;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 16, nullable = false)
    private Gender gender;

    @NotBlank
    @Size(max = 20)
    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @NotBlank
    @Email
    @Size(max = 128)
    @Column(name = "email", length = 128, nullable = false)
    private String email;

    @Size(max = 255)
    @Column(name = "address", length = 255)
    private String address;

    // status
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotBlank
    @Column(name = "created_by", length = 36, nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "run_at", columnDefinition = "datetime(6)")
    private LocalDateTime runAt;

    @Column(name = "run_by", length = 36)
    private String runBy;

    @Column(name = "updated_at", columnDefinition = "datetime(6)")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    // review
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 24, nullable = false)
    private ReviewStatus reviewStatus;

    @Column(name = "reviewed_at", columnDefinition = "datetime(6)")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_mode", length = 8)
    private ReviewMode reviewMode;

    // soft delete
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at", columnDefinition = "datetime(6)")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    @OneToMany(mappedBy = "orderRef", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<TestResult> results;

    @OneToMany(mappedBy = "orderRef", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<OrderEventLog> events;

    @OneToMany(mappedBy = "orderRef", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<TestResultAdjustLog> adjustLogs;

    @OneToMany(mappedBy = "orderRef", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<PrintSnapshot> printSnapshots;

    @OneToMany(mappedBy = "printOrderRef", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<ReportJob> relatedPrintJobs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_type_id", nullable = false)
    private TestType testTypeRef;

    @NotBlank
    @Size(max = 36)
    @Column(name = "test_type_id_snapshot", length = 36, nullable = false)
    private String testTypeIdSnapshot;

    @NotBlank
    @Size(max = 100)
    @Column(name = "test_type_name_snapshot", length = 100, nullable = false)
    private String testTypeNameSnapshot;

    @PrePersist
    void pp() {
        if (orderId == null) orderId = TestOrderGenerator.generateTestOrderId();
        if (orderCode == null) orderCode = TestOrderGenerator.generateTestOrderCode();
        if (status == null) status = OrderStatus.PENDING;
        if (reviewStatus == null) reviewStatus = ReviewStatus.NONE;
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    void pu() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}

