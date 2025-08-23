package com.gt.visitor_pass_service.repository;

import com.gt.visitor_pass_service.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop10ByTenantIdAndPassIdIsNotNullOrderByTimestampDesc(Long tenantId);
}
