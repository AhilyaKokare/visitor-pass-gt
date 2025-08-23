package com.gt.notification_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "email_audit_logs")
public class EmailAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String correlationId;
    private Long associatedPassId;
    private String recipientAddress;
    private String subject;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;
    @Enumerated(EnumType.STRING)
    private EmailStatus status;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}

// Create an Enum EmailStatus.java: PENDING, SENT, FAILED