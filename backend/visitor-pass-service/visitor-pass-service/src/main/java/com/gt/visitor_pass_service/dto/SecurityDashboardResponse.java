package com.gt.visitor_pass_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityDashboardResponse {
    private Long passId;
    private String visitorName;
    private String passCode;
    private String status;
    private LocalDateTime visitDateTime;
    private String employeeHostName;
}