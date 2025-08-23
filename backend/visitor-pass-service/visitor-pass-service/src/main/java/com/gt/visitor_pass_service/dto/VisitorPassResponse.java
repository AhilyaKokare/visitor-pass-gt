package com.gt.visitor_pass_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // This prevents fields with null values from being sent in the JSON response
public class VisitorPassResponse {
    private Long id;
    private Long tenantId;
    private String visitorName;
    private String visitorEmail;        // <-- ADDED
    private String visitorPhone;        // <-- ADDED
    private String purpose;             // <-- ADDED
    private String status;
    private String passCode;
    private LocalDateTime visitDateTime;
    private String createdByEmployeeName;
    private String approvedBy;          // <-- ADDED
    private String rejectionReason;     // <-- ADDED
}