package com.gt.visitor_pass_service.dto;

import com.gt.visitor_pass_service.model.AuditLog;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TenantDashboardResponse {
    private TenantDashboardStats stats;
    private List<VisitorPassResponse> recentPasses; // Last 10 passes
    private List<AuditLog> recentPassActivity; // Last 10 pass-related business events
    private List<EmailAuditLogResponse> recentEmailActivity; // Last 10 email events
}