package com.gt.visitor_pass_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantActivityDTO {
    private Long tenantId;
    private String tenantName;
    private String locationDetails;
    private long userCount;
    private long passesToday;
    private long totalPassesAllTime;
}