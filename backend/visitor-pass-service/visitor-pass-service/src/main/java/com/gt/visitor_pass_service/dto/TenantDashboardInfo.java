package com.gt.visitor_pass_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TenantDashboardInfo {
    // Tenant Info
    private Long tenantId;
    private String tenantName;
    private String locationDetails;
    private String createdBy;
    private LocalDateTime createdAt;

    // Tenant Admin Info
    private String adminName;
    private String adminEmail;
    private String adminContact;
    private boolean adminIsActive;
}