package com.gt.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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