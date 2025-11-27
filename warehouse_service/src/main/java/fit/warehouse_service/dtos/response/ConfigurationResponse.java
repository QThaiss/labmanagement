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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
public class ConfigurationResponse {
    private String id;
    private String name;
    private String description;

    // Các trường mới
    private String configType;
    private String instrumentModel;
    private String instrumentType;
    private String version;

    // Trả về Object (Map) thay vì String để FE dễ render JSON
    private Map<String, Object> settings;

    private LocalDateTime createdAt;
    private String createdByUserId;
    private LocalDateTime updatedAt;
    private String updatedByUserId;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
}