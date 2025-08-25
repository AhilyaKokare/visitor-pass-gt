package com.gt.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassApprovedEvent implements Serializable {
    private Long passId;
    private Long tenantId;
    private String visitorName;
    private String visitorEmail;
    private String employeeEmail;
    
    // VVV --- THESE ARE THE MISSING FIELDS --- VVV
    private String passCode;
    private LocalDateTime visitDateTime;
    private String employeeName;
}