package com.gt.visitor_pass_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassExpiredEvent implements Serializable {
    private Long passId;
    private String visitorName;
    private LocalDateTime visitDateTime;
    private String employeeEmail;
    private String tenantAdminEmail; // Can be null if no admin is found
    private Long tenantId;
}