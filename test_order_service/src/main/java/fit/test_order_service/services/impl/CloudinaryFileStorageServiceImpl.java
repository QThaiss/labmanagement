/*
 * @ (#) CloudinaryFileStorageServiceImpl.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import fit.test_order_service.enums.StorageType;
import fit.test_order_service.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class CloudinaryFileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;


    @Override
    public String storeFile(byte[] fileBytes, String requestedFileName, String targetFolder, String contentType, String uploaderUserId) {

        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", "raw",
                    "public_id", requestedFileName,
                    "overwrite", true,
                    "folder", targetFolder
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(fileBytes, options);

            String url = (String) uploadResult.get("secure_url");
            if (url == null) {
                url = (String) uploadResult.get("url");
            }

            log.info("File {} uploaded to Cloudinary folder [{}]. URL: {}", requestedFileName, targetFolder, url);
            return url; // Trả về URL an toàn

        } catch (IOException e) {
            log.error("Could not store file {} to Cloudinary. Error: {}", requestedFileName, e.getMessage(), e);
            throw new RuntimeException("Could not store file " + requestedFileName + ". Please try again!", e);
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.CLOUDINARY;
    }
}
