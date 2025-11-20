/*
 * @ (#) OrderComment.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.CommentTargetType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_comment", indexes = {
        @Index(name = "idx_cmt_target_time", columnList = "targetId, created_at DESC"),
        @Index(name = "idx_cmt_deleted", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class OrderComment {
    @Id
    @Column(name = "comment_id", length = 36, nullable = false, updatable = false)
    private String commentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20, nullable = false)
    private CommentTargetType targetType;

    @NotBlank
    @Column(name = "target_id", length = 36, nullable = false)
    private String targetId;

    @NotBlank
    @Column(name = "author_user_id", length = 36, nullable = false)
    private String authorUserId;

    @NotBlank
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "datetime(6)")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "is_edited", nullable = false)
    private boolean edited;

    @Column(name = "edit_count", columnDefinition = "smallint unsigned", nullable = false)
    private Integer editCount;

    @Column(name = "deleted_at", columnDefinition = "datetime(6)")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    @Size(max = 255)
    @Column(name = "delete_reason", length = 255)
    private String deleteReason;

    @Column(name = "parent_id", length = 36)
    private String parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private OrderComment parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC") // Thường reply sẽ sắp xếp tăng dần
    private List<OrderComment> replies;

    @OneToMany(mappedBy = "commentRef", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<OrderCommentEditLog> editLogs;

    @PrePersist
    void pp() {
        if (commentId == null) commentId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
        if (editCount == null) editCount = 0;
    }

    @PreUpdate
    void pu() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}