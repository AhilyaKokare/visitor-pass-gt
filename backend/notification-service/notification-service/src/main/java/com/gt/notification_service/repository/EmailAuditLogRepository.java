package com.gt.notification_service.repository;

import com.gt.notification_service.model.EmailAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailAuditLogRepository extends JpaRepository<EmailAuditLog, Long> {
    List<EmailAuditLog> findByAssociatedPassIdIn(List<Long> passIds);
}