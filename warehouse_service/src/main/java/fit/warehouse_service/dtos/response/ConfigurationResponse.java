/*
 * @ (#) ConfigurationResponse.java    1.0    03/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 03/11/2025
 * @version: 1.0
 */

import fit.warehouse_service.enums.DataType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi tiết của một Configuration Setting.
 */
@Getter
@Setter
@Builder
public class ConfigurationResponse {
    private String id;
    private String name;
    private String description;
    private String dataType;
    private Object value;
    private LocalDateTime createdAt;
    private String createdByUserId;
    private LocalDateTime updatedAt;
    private String updatedByUserId;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
}