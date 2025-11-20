/*
 * @ (#) FileStorageService.java    1.0    22/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 22/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.StorageType;

public interface FileStorageService {

    /**
     * Lưu trữ một file.
     *
     * @param directoryPath Thư mục con mong muốn (ví dụ: "pdf_exports" hoặc "excel_exports").
     * @param fileBytes  Dữ liệu byte của file.
     * @param fileName   Tên file (ví dụ: "report.pdf").
     * @param mimeType   Loại MIME (ví dụ: "application/pdf").
     * @param uploaderId ID của người tải file lên.
     * @return Một key duy nhất hoặc URL đại diện cho file đã lưu.
     */
    String storeFile(byte[] fileBytes,
                     String fileName,
                     String directoryPath,
                     String mimeType,
                     String uploaderId);

    /**
     * Lấy loại hình lưu trữ đang được sử dụng (LOCAL, S3,...).
     */
    StorageType getStorageType();
}
