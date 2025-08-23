package com.gt.visitor_pass_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmailAuditLogResponse {
    private Long id;
    private Long associatedPassId;
    private String recipientAddress;
    private String subject;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String failureReason;
}