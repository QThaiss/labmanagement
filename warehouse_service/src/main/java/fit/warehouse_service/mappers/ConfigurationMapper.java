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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.warehouse_service.dtos.request.CreateConfigurationRequest;
import fit.warehouse_service.dtos.response.ConfigurationResponse;
import fit.warehouse_service.entities.ConfigurationSetting;
import fit.warehouse_service.enums.DataType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConfigurationMapper {

    private final ObjectMapper objectMapper; // Inject Jackson ObjectMapper

    public ConfigurationSetting toEntity(CreateConfigurationRequest request) {
        if (request == null) {
            return null;
        }

        ConfigurationSetting entity = new ConfigurationSetting();

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        // Map các trường mới từ Request
        entity.setConfigType(request.getConfigType());
        entity.setInstrumentModel(request.getInstrumentModel());
        entity.setInstrumentType(request.getInstrumentType());
        entity.setVersion(request.getVersion());

        // Chuyển đổi Map settings sang JSON String để lưu DB
        try {
            if (request.getSettings() != null) {
                entity.setSettings(objectMapper.writeValueAsString(request.getSettings()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting settings map to JSON string", e);
        }

        return entity;
    }

    public ConfigurationResponse toResponse(ConfigurationSetting entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> settingsMap = null;
        // Chuyển đổi JSON String từ DB sang Map để trả về
        try {
            if (entity.getSettings() != null) {
                settingsMap = objectMapper.readValue(entity.getSettings(), new TypeReference<Map<String, Object>>() {
                });
            }
        } catch (JsonProcessingException e) {
            // Log error nhưng không throw để vẫn trả về các thông tin khác
            System.err.println("Error parsing settings JSON: " + e.getMessage());
        }

        return ConfigurationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .configType(entity.getConfigType())
                .instrumentModel(entity.getInstrumentModel())
                .instrumentType(entity.getInstrumentType())
                .version(entity.getVersion())
                .settings(settingsMap) // Trả về Map
                .createdAt(entity.getCreatedAt())
                .createdByUserId(entity.getCreatedByUserId())
                .updatedAt(entity.getUpdatedAt())
                .updatedByUserId(entity.getUpdatedByUserId())
                .deletedAt(entity.getDeletedAt())
                .isDeleted(entity.isDeleted())
                .build();
    }

    // Method update reuse logic (có thể gọi lại toResponse hoặc viết riêng nếu cần logic khác)
    public ConfigurationResponse toResponseUpdate(ConfigurationSetting entity) {
        return toResponse(entity);
    }
}