package com.gt.visitor_pass_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantDashboardStats {
    private long pendingPasses;
    private long approvedPassesToday;
    private long checkedInVisitors;
    private long completedPassesToday;
}