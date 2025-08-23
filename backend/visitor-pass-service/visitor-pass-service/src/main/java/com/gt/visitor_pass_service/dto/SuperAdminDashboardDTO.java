package com.gt.visitor_pass_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor // <-- ADD THIS
@AllArgsConstructor
public class SuperAdminDashboardDTO {
    private GlobalStatsDTO globalStats;
    private List<TenantActivityDTO> tenantActivity;
    private List<VisitorPassResponse> recentPassesAcrossAllTenants; // Last 5-10 recent passes system-wide
}