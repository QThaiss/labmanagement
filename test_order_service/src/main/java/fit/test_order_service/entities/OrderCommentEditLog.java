/*
 * @ (#) OrderCommentEditLog.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "order_comment_edit_log", indexes = {
        @Index(name = "idx_cmt_edit_comment", columnList = "comment_id"),
        @Index(name = "idx_cmt_edit_target", columnList = "target_id"), // Sửa index
        @Index(name = "idx_cmt_edit_time", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCommentEditLog {
    @Id
    @Column(name = "edit_id", length = 36, nullable = false, updatable = false)
    private String editId;

    @NotBlank
    @Column(name = "comment_id", length = 36, nullable = false)
    private String commentId;

    @NotBlank
    @Column(name = "target_id", length = 36, nullable = false)
    private String targetId; // Thay thế cho orderId/resultId

    @NotBlank
    @Column(name = "target_type", length = 20, nullable = false)
    private String targetType; // Thêm targetType

    @NotBlank
    @Column(name = "editor_user_id", length = 36, nullable = false)
    private String editorUserId;

    @Lob
    @Column(name = "before_content")
    private String beforeContent;

    @NotBlank
    @Lob
    @Column(name = "after_content", nullable = false)
    private String afterContent;

    @Size(max = 255)
    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime createdAt;

    /* Relations */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", referencedColumnName = "comment_id",
            foreignKey = @ForeignKey(name = "fk_cmt_edit_comment"), insertable = false, updatable = false)
    private OrderComment commentRef;

    @PrePersist
    void pp() {
        if (editId == null) editId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}