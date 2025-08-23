package com.gt.visitor_pass_service.service;

import com.gt.visitor_pass_service.dto.GlobalStatsDTO;
import com.gt.visitor_pass_service.dto.SuperAdminDashboardDTO;
import com.gt.visitor_pass_service.dto.TenantActivityDTO;
import com.gt.visitor_pass_service.dto.VisitorPassResponse;
import com.gt.visitor_pass_service.model.Tenant;
import com.gt.visitor_pass_service.repository.TenantRepository;
import com.gt.visitor_pass_service.repository.UserRepository;
import com.gt.visitor_pass_service.repository.VisitorPassRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperAdminDashboardService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final VisitorPassRepository passRepository;
    private final VisitorPassService visitorPassService; // For re-using the DTO mapper

    public SuperAdminDashboardService(TenantRepository tenantRepository,
                                      UserRepository userRepository,
                                      VisitorPassRepository passRepository,
                                      VisitorPassService visitorPassService) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passRepository = passRepository;
        this.visitorPassService = visitorPassService;
    }

    public SuperAdminDashboardDTO getDashboardData() {
        GlobalStatsDTO globalStats = buildGlobalStats();
        List<TenantActivityDTO> tenantActivity = buildTenantActivity();
        List<VisitorPassResponse> recentPasses = getRecentPasses();

        return SuperAdminDashboardDTO.builder()
                .globalStats(globalStats)
                .tenantActivity(tenantActivity)
                .recentPassesAcrossAllTenants(recentPasses)
                .build();
    }

    private GlobalStatsDTO buildGlobalStats() {
        return GlobalStatsDTO.builder()
                .totalTenants(tenantRepository.count() - 1) // Subtract 1 for the "Global" tenant
                .totalUsers(userRepository.count())
                .totalPassesIssued(passRepository.count())
                .activePassesToday(passRepository.countActivePassesForToday())
                .build();
    }

    private List<TenantActivityDTO> buildTenantActivity() {
        List<Tenant> tenants = tenantRepository.findAll().stream()
                .filter(tenant -> !"Global Administration".equals(tenant.getName()))
                .collect(Collectors.toList());

        return tenants.stream()
                .map(tenant -> TenantActivityDTO.builder()
                        .tenantId(tenant.getId())
                        .tenantName(tenant.getName())
                        .locationDetails(tenant.getLocationDetails()) // <-- ADD THIS LINE
                        .userCount(userRepository.countByTenantId(tenant.getId()))
                        .passesToday(passRepository.countPassesForTenantToday(tenant.getId()))
                        .totalPassesAllTime(passRepository.countByTenantId(tenant.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<VisitorPassResponse> getRecentPasses() {
        return passRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(visitorPassService::mapToResponse) // Reuse the mapper
                .collect(Collectors.toList());
    }

    public Page<TenantActivityDTO> getPaginatedTenantActivity(Pageable pageable) {
        List<Tenant> allTenants = tenantRepository.findAll();

        // Filter out the "Global Administration" tenant and build activity data
        List<TenantActivityDTO> tenantActivity = allTenants.stream()
                .filter(tenant -> !"Global Administration".equals(tenant.getName()))
                .map(tenant -> TenantActivityDTO.builder()
                        .tenantId(tenant.getId())
                        .tenantName(tenant.getName())
                        .locationDetails(tenant.getLocationDetails())
                        .userCount(userRepository.countByTenantId(tenant.getId()))
                        .passesToday(passRepository.countActivePassesForToday()) // Use existing method
                        .totalPassesAllTime(passRepository.countByTenantId(tenant.getId()))
                        .build())
                .collect(Collectors.toList());

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), tenantActivity.size());

        List<TenantActivityDTO> pageContent = tenantActivity.subList(start, end);

        return new PageImpl<>(pageContent, pageable, tenantActivity.size());
    }
}