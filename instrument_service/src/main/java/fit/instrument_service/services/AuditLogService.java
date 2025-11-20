package fit.instrument_service.services;

import fit.instrument_service.entities.BaseDocument;
import fit.instrument_service.enums.AuditAction;

import java.util.Map;

public interface AuditLogService {
    void logAction(AuditAction action, String entityId, String entityType, Map<String, Object> details);
}