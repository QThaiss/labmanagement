/*
 * @ (#) ConfigurationServiceImpl.java    1.0    03/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 03/11/2025
 * @version: 1.0
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.warehouse_service.constants.SortFields;
import fit.warehouse_service.dtos.request.CreateConfigurationRequest;
import fit.warehouse_service.dtos.request.ModifyConfigurationRequest;
import fit.warehouse_service.dtos.response.ConfigurationResponse;
import fit.warehouse_service.dtos.response.FilterInfo;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.entities.ConfigurationSetting;
import fit.warehouse_service.enums.WarehouseActionType;
import fit.warehouse_service.events.ConfigurationCreatedEvent;
import fit.warehouse_service.events.ConfigurationDeletedEvent;
import fit.warehouse_service.events.ConfigurationUpdatedEvent;
import fit.warehouse_service.exceptions.DuplicateResourceException;
import fit.warehouse_service.exceptions.NotFoundException;
import fit.warehouse_service.exceptions.ResourceNotFoundException;
import fit.warehouse_service.mappers.ConfigurationMapper;
import fit.warehouse_service.repositories.ConfigurationSettingRepository;
import fit.warehouse_service.services.ConfigurationService;
import fit.warehouse_service.services.EventPublisherService;
import fit.warehouse_service.services.WarehouseEventLogService;
import fit.warehouse_service.specifications.ConfigurationSpecification;
import fit.warehouse_service.utils.SecurityUtils;
import fit.warehouse_service.utils.SortUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationSettingRepository configurationSettingRepository;
    private final WarehouseEventLogService logService;
    private final ConfigurationMapper configurationMapper;
    private final ConfigurationSpecification configurationSpecification;
    private final EventPublisherService eventPublisherService;
    private final ObjectMapper objectMapper; // Inject ObjectMapper

    @Override
    @Transactional
    public ConfigurationResponse createConfiguration(CreateConfigurationRequest request) {
        log.info("Creating new configuration with name: {}", request.getName());
        if (configurationSettingRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Configuration with name '" + request.getName() + "' already exists.");
        }

        ConfigurationSetting newSetting = configurationMapper.toEntity(request);
        ConfigurationSetting savedSetting = configurationSettingRepository.save(newSetting);

        log.info("Successfully created configuration with id: {}", savedSetting.getId());

        // Ghi log audit
        String currentUserId = SecurityUtils.getCurrentUserId();
        logService.logEvent(
                WarehouseActionType.CONFIG_CREATED,
                savedSetting.getId(),
                "Configuration created: " + savedSetting.getName() + " (" + savedSetting.getConfigType() + ")",
                currentUserId
        );

        // Publish event - Sử dụng Map settings từ request để gửi đi
        ConfigurationCreatedEvent event = new ConfigurationCreatedEvent(
                savedSetting.getId(),
                savedSetting.getName(),
                savedSetting.getConfigType(),
                savedSetting.getInstrumentModel(),
                savedSetting.getInstrumentType(),
                savedSetting.getVersion(),
                request.getSettings(), // Truyền Map settings
                savedSetting.getDescription()
        );
        eventPublisherService.publishConfigurationCreated(event);

        return configurationMapper.toResponse(savedSetting);
    }

    @Override
    @Transactional
    public ConfigurationResponse modifyConfiguration(String configurationId, ModifyConfigurationRequest request) {
        // Kiểm tra tồn tại
        ConfigurationSetting existingConfig = configurationSettingRepository
                .findByIdAndDeletedAtIsNull(configurationId)
                .orElseThrow(() -> new NotFoundException(
                        "Configuration with ID '" + configurationId + "' not found or has been deleted."));

        // Convert Settings Map sang JSON String để so sánh và lưu
        String newSettingsJson;
        try {
            newSettingsJson = objectMapper.writeValueAsString(request.getSettings());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid settings format", e);
        }

        String oldSettingsJson = existingConfig.getSettings();

        // Kiểm tra xem giá trị có thực sự thay đổi không
        boolean isSettingsChanged = !newSettingsJson.equals(oldSettingsJson);
        boolean isVersionChanged = request.getVersion() != null && !request.getVersion().equals(existingConfig.getVersion());

        if (!isSettingsChanged && !isVersionChanged) {
            log.info("No modification applied for config {}", existingConfig.getName());
            return configurationMapper.toResponse(existingConfig);
        }

        // Cập nhật
        if (isSettingsChanged) existingConfig.setSettings(newSettingsJson);
        if (isVersionChanged) existingConfig.setVersion(request.getVersion());

        existingConfig.setUpdatedAt(LocalDateTime.now());
        existingConfig.setUpdatedByUserId(SecurityUtils.getCurrentUserId());

        ConfigurationSetting updatedConfig = configurationSettingRepository.save(existingConfig);

        // Ghi log
        String logDetails = "Modified config " + updatedConfig.getName() + ". Reason: " + request.getModificationReason();
        logService.logEvent(
                WarehouseActionType.CONFIG_UPDATED,
                updatedConfig.getId(),
                "ConfigurationSetting",
                logDetails
        );

        // Publish event update sang Instrument Service
        ConfigurationUpdatedEvent event = new ConfigurationUpdatedEvent(
                updatedConfig.getId(),
                updatedConfig.getName(),
                updatedConfig.getVersion(),
                request.getSettings(), // Truyền Map settings mới nhất
                request.getModificationReason()
        );
        eventPublisherService.publishConfigurationUpdated(event);

        return configurationMapper.toResponseUpdate(updatedConfig);
    }

    @Override
    @Transactional(readOnly = true)
    // Cập nhật tham số: thay DataType bằng String configType
    public PageResponse<ConfigurationResponse> getAllConfigurations(int page, int size, String[] sort, String search, String configType, LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving configurations: search='{}', type='{}'", search, configType);

        Sort validSort = SortUtils.buildSort(
                sort,
                SortFields.CONFIGURATION_SORT_FIELDS,
                SortFields.DEFAULT_CONFIGURATION_SORT
        );

        Pageable pageable = PageRequest.of(page, size, validSort);

        // Sử dụng Specification đã update
        Specification<ConfigurationSetting> spec = configurationSpecification.build(search, configType, startDate, endDate);

        Page<ConfigurationSetting> pageConfigs = configurationSettingRepository.findAll(spec, pageable);
        Page<ConfigurationResponse> dtoPage = pageConfigs.map(configurationMapper::toResponseUpdate);

        FilterInfo filterInfo = FilterInfo.builder()
                .search(search)
                .configType(configType)

                .startDate(startDate)
                .endDate(endDate)
                .build();

        return PageResponse.from(dtoPage, filterInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigurationResponse getConfigurationById(String configurationId) {
        ConfigurationSetting configuration = configurationSettingRepository
                .findByIdAndDeletedAtIsNull(configurationId)
                .orElseThrow(() -> new NotFoundException(
                        "Configuration with ID '" + configurationId + "' not found."));

        return configurationMapper.toResponseUpdate(configuration);
    }

    @Override
    @Transactional
    public void deleteConfiguration(String id) {
        ConfigurationSetting configuration = configurationSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration with id " + id + " not found."));

        // Soft delete logic (hoặc hard delete tùy policy)
        // Ở đây repository dùng delete() là hard delete, nếu muốn soft delete cần setDeletedAt
        configuration.setDeletedAt(LocalDateTime.now());
        configuration.setDeleted(true);
        configurationSettingRepository.save(configuration);

        log.info("Successfully deleted configuration with id: {}", id);

        logService.logEvent(
                WarehouseActionType.CONFIG_DELETED,
                id,
                "Configuration deleted: " + configuration.getName(),
                SecurityUtils.getCurrentUserId()
        );

        eventPublisherService.publishConfigurationDeleted(new ConfigurationDeletedEvent(id));
    }

    // Các hàm helper cũ (convertValueToString, validateValueFormat) đã được loại bỏ
    // vì logic validation đã được chuyển sang Jackson ObjectMapper và DTO validation
}
