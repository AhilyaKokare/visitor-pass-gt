package com.gt.visitor_pass_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatsDTO {
    private long totalTenants;
    private long totalUsers;
    private long totalPassesIssued;
    private long activePassesToday; // Approved or Checked-In for the current day
}