/*
 * @ (#) ReportFileStore.java    1.0    11/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 11/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.StorageType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report_file_store", indexes = {
        @Index(name = "idx_file_creator_time", columnList = "created_by, created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportFileStore {
    @Id
    @Column(name = "file_id", length = 36, nullable = false, updatable = false)
    private String fileId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", length = 16, nullable = false)
    private StorageType storageType;

    @Size(max = 128)
    @Column(name = "bucket", length = 128)
    private String bucket;

    @NotBlank
    @Size(max = 255)
    @Column(name = "object_key", length = 255, nullable = false)
    private String objectKey;

    @NotBlank
    @Size(max = 128)
    @Column(name = "file_name", length = 128, nullable = false)
    private String fileName;

    @NotBlank
    @Size(max = 128)
    @Column(name = "mime_type", length = 64, nullable = false)
    private String mimeType;

    @Column(name = "byte_size")
    private Long byteSize;

    @Size(max = 32)
    @Column(name = "checksum_md5", length = 32)
    private String checksumMd5;

    @NotBlank
    @Column(name = "created_by", length = 36, nullable = false)
    private String createdBy;

    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", columnDefinition = "datetime(6)")
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "resultFile", fetch = FetchType.LAZY)
    private List<ReportJob> jobs;

    @PrePersist
    void pp() {
        if (fileId == null) fileId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}

