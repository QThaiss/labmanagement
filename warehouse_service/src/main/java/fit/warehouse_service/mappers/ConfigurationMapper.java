/*
 * @ (#) ConfigurationMapper.java    1.0    03/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.mappers;/*
 * @description:
 * @author: Bao Thong
 * @date: 03/11/2025
 * @version: 1.0
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.warehouse_service.dtos.request.CreateConfigurationRequest;
import fit.warehouse_service.dtos.response.ConfigurationResponse;
import fit.warehouse_service.entities.ConfigurationSetting;
import fit.warehouse_service.enums.DataType;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationMapper {

    public ConfigurationSetting toEntity(CreateConfigurationRequest request) {
        if (request == null) {
            return null;
        }

        ConfigurationSetting entity = new ConfigurationSetting();

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setDataType(request.getDataType());
        entity.setValue(request.getValue());

        // Các trường audit (createdAt, createdByUserId...) sẽ được
        // JPA Auditing tự động xử lý khi lưu (save).

        return entity;
    }

    public ConfigurationResponse toResponse(ConfigurationSetting entity) {
        if (entity == null) {
            return null;
        }

        return ConfigurationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .dataType(entity.getDataType() != null ? entity.getDataType().name() : null)
                .value(entity.getValue())
                .createdAt(entity.getCreatedAt())
                .createdByUserId(entity.getCreatedByUserId())
                .build();
    }

    public ConfigurationResponse toResponseUpdate(ConfigurationSetting entity) {
        if (entity == null) {
            return null;
        }

        Object parsedValue = parseValueByType(entity.getValue(), entity.getDataType());

        return ConfigurationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .dataType(entity.getDataType() != null ? entity.getDataType().name() : null)
                .value(parsedValue)
                .createdAt(entity.getCreatedAt())
                .createdByUserId(entity.getCreatedByUserId())
                .updatedAt(entity.getUpdatedAt())
                .updatedByUserId(entity.getUpdatedByUserId())
                .deletedAt(entity.getDeletedAt())
                .isDeleted(entity.isDeleted())
                .build();
    }

    private Object parseValueByType(String value, DataType dataType) {
        if (value == null) return null;

        try {
            switch (dataType) {
                case INTEGER -> {
                    return Integer.parseInt(value);
                }
                case BOOLEAN -> {
                    return Boolean.parseBoolean(value);
                }
                case JSON -> {
                    return new ObjectMapper().readValue(value, Object.class);
                }
                default -> {
                    return value;
                }
            }
        } catch (Exception e) {
            return value; // fallback nếu parse lỗi
        }
    }
}
