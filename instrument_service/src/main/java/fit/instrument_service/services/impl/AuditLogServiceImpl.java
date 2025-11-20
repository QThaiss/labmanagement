package fit.instrument_service.services.impl;

import fit.instrument_service.entities.AuditLog;
import fit.instrument_service.enums.AuditAction;
import fit.instrument_service.repositories.AuditLogRepository;
import fit.instrument_service.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(AuditAction action, String entityId, String entityType, Map<String, Object> details) {
        AuditLog logEntry = new AuditLog();
        logEntry.setAction(action);
        logEntry.setEntityId(entityId);
        logEntry.setEntityType(entityType);
        logEntry.setDetails(details);

        // Ghi chú: createdAt và createdBy sẽ được tự động điền bởi
        // MongoAuditing (BaseDocument và AuditorAwareImpl)

        auditLogRepository.save(logEntry);
    }
}