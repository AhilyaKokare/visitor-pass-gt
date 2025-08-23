package com.gt.visitor_pass_service.service;

import com.gt.visitor_pass_service.model.AuditLog;
import com.gt.visitor_pass_service.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logEvent(String action, Long userId, Long tenantId, Long passId) {
        AuditLog log = new AuditLog();
        log.setActionDescription(action);
        log.setUserId(userId);
        log.setTenantId(tenantId);
        log.setPassId(passId);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}