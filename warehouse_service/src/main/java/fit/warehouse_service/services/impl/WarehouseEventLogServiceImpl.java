/*
 * @ (#) WarehouseEventLogServiceImpl.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.warehouse_service.entities.*;
import fit.warehouse_service.enums.InstrumentStatus;
import fit.warehouse_service.enums.WarehouseActionType;
import fit.warehouse_service.repositories.WarehouseEventLogRepository;
import fit.warehouse_service.services.WarehouseEventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseEventLogServiceImpl implements WarehouseEventLogService {

    private final WarehouseEventLogRepository logRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void logEvent(WarehouseActionType action, String entityId, String entityType, String details) {
        // ... (no change in this method)
        try {
            WarehouseEventLog eventLog = new WarehouseEventLog();
            eventLog.setAction(action);
            eventLog.setEntityId(entityId);
            eventLog.setEntityType(entityType);
            eventLog.setDetails(details);
            logRepository.save(eventLog);
        } catch (Exception e) {
            log.error("Failed to save warehouse event log: {}", e.getMessage(), e);
        }
    }

    @Override
    public String createInstrumentCreatedDetails(Instrument instrument) {
        Map<String, Object> details = new HashMap<>();
        details.put("name", instrument.getName());
        details.put("status", instrument.getStatus().name());

        // --- Add New Fields to Log ---
        details.put("ipAddress", instrument.getIpAddress());
        details.put("port", instrument.getPort());
        if (instrument.getProtocolType() != null) { // Check if protocolType is set
            details.put("protocolType", instrument.getProtocolType().name());
        }

        // --- Existing Fields ---
        if (instrument.getCompatibleReagents() != null) {
            details.put("compatibleReagentIds", instrument.getCompatibleReagents().stream()
                    .map(reagent -> reagent.getId())
                    .collect(Collectors.toSet()));
        }

        if (instrument.getConfigurations() != null) {
            details.put("configurationSettingIds", instrument.getConfigurations().stream()
                    .map(config -> config.getId())
                    .collect(Collectors.toSet()));
        }

        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize instrument details for logging: {}", e.getMessage());
            return "{\"error\": \"Could not serialize details\"}";
        }
    }

    @Override
    public String createInstrumentActivationDetails(Instrument instrument, String action, String reason, boolean previousActiveState, InstrumentStatus previousStatus) {
        Map<String, Object> details = new HashMap<>();
        details.put("instrumentId", instrument.getId());
        details.put("instrumentName", instrument.getName());
        details.put("action", action);
        details.put("previousActiveState", previousActiveState);
        details.put("currentActiveState", instrument.isActive());
        details.put("previousStatus", previousStatus.name());
        details.put("currentStatus", instrument.getStatus().name());
        if (reason != null && !reason.trim().isEmpty()) {
            details.put("reason", reason);
        }

        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize instrument activation details for logging: {}", e.getMessage());
            return "{\"error\": \"Could not serialize details\"}";
        }
    }

    @Override
    public String createScheduledDeletionDetails(Instrument instrument, LocalDateTime scheduledTime, String reason) {
        return String.format(
                "Instrument '%s' (ID: %s) has been scheduled for automatic deletion on %s. Reason: %s",
                instrument.getName(),
                instrument.getId(),
                scheduledTime.toString(),
                reason != null ? reason : "Deactivated for 3 months"
        );
    }

    @Override
    public String createDeletionCancellationDetails(String instrumentId, LocalDateTime originalScheduledTime) {
        return String.format(
                "Scheduled deletion for instrument %s (originally scheduled for %s) has been cancelled due to reactivation",
                instrumentId,
                originalScheduledTime.toString()
        );
    }

    @Override
    public String createInstrumentDeletionDetails(Instrument instrument) {
        return String.format(
                "Instrument '%s' (ID: %s) has been permanently deleted from the system after being deactivated for 3 months. " +
                        "IP: %s, Port: %s, Protocol: %s",
                instrument.getName(),
                instrument.getId(),
                instrument.getIpAddress(),
                instrument.getPort(),
                instrument.getProtocolType()
        );
    }

    @Override
    public String createReagentReceivedDetails(ReagentSupplyHistory history) {
        try {
            Map<String, Object> details = Map.ofEntries(
                    Map.entry("historyId", history.getId()),
                    Map.entry("reagentTypeId", history.getReagentType() != null ? history.getReagentType().getId() : "N/A"),
                    Map.entry("reagentName", history.getReagentType() != null ? history.getReagentType().getName() : "N/A"),
                    Map.entry("lotNumber", history.getLotNumber()),
                    Map.entry("quantityReceived", history.getQuantityReceived()),
                    Map.entry("unitOfMeasure", history.getUnitOfMeasure()),
                    Map.entry("vendorName", history.getVendor() != null ? history.getVendor().getName() : "N/A"),
                    Map.entry("poNumber", history.getPoNumber()),
                    Map.entry("receiptDate", history.getReceiptDate() != null ? history.getReceiptDate().toString() : "N/A"),
                    Map.entry("expirationDate", history.getExpirationDate() != null ? history.getExpirationDate().toString() : "N/A"),
                    Map.entry("receivedByUserId", history.getReceivedByUserId()), // Explicit receiver
                    Map.entry("loggedByUserId", history.getCreatedByUserId()), // Audit user
                    Map.entry("loggedAt", history.getCreatedAt() != null ? history.getCreatedAt().toString() : "N/A"), // Audit time
                    Map.entry("initialStorageLocation", history.getInitialStorageLocation()),
                    Map.entry("status", history.getStatus().name())
            );
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize reagent received details for logging history ID {}: {}", history.getId(), e.getMessage());
            return "{\"error\": \"Could not serialize details\"}";
        }
    }

    @Override
    public String createReagentUsageDetails(ReagentUsageHistory history) {
        try {
            Map<String, Object> details = Map.ofEntries(
                    Map.entry("usageHistoryId", history.getId()),
                    Map.entry("reagentLotId", history.getReagentLot().getId()),
                    Map.entry("reagentName", history.getReagentLot().getReagentType().getName()),
                    Map.entry("lotNumber", history.getReagentLot().getLotNumber()),
                    Map.entry("instrumentId", history.getInstrument() != null ? history.getInstrument().getId() : "N/A"),
                    Map.entry("instrumentName", history.getInstrument() != null ? history.getInstrument().getName() : "N/A"),
                    Map.entry("quantityUsed", history.getQuantityUsed()),
                    Map.entry("action", history.getAction()),
                    Map.entry("usedByUserId", history.getCreatedByUserId()),
                    Map.entry("timestamp", history.getCreatedAt().toString())
            );
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize reagent usage details for logging history ID {}: {}", history.getId(), e.getMessage());
            return "{\"error\": \"Could not serialize details\"}";
        }
    }

    @Override
    public String createConfigurationCreatedDetails(ConfigurationSetting config) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("id", config.getId());
            details.put("name", config.getName());
            details.put("value", config.getValue());
            details.put("dataType", config.getDataType().name());
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize config details for logging: {}", e.getMessage());
            return "{\"error\": \"Could not serialize details\"}";
        }
    }

    @Override
    public String createConfigurationModifiedDetails(
            ConfigurationSetting config,
            String oldValue,
            String newValue,
            String reason) {

        StringBuilder details = new StringBuilder();
        details.append("Configuration Modified - ");
        details.append("Name: ").append(config.getName()).append(", ");
        details.append("Old Value: '").append(oldValue).append("', ");
        details.append("New Value: '").append(newValue).append("'");

        if (StringUtils.hasText(reason)) {
            details.append(", Reason: ").append(reason);
        }

        return details.toString();
    }
}
