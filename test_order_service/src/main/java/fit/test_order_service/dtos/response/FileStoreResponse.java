/*
 * @ (#) FileStoreResponse.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.StorageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileStoreResponse {
    private String fileId;
    private StorageType storageType;
    private String objectKey;
    private String fileName;
    private String mimeType;
    private Long byteSize;
    private String createdBy;
    private LocalDateTime createdAt;
}
