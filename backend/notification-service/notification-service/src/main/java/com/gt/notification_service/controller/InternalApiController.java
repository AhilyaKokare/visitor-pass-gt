package com.gt.notification_service.controller;

import com.gt.notification_service.dto.EmailAuditLogResponse;
import com.gt.notification_service.model.EmailAuditLog;
import com.gt.notification_service.repository.EmailAuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/internal")
public class InternalApiController {

    private final EmailAuditLogRepository emailAuditLogRepository;

    public InternalApiController(EmailAuditLogRepository emailAuditLogRepository) {
        this.emailAuditLogRepository = emailAuditLogRepository;
    }

    @PostMapping("/email-logs/by-pass-ids")
    public ResponseEntity<List<EmailAuditLogResponse>> getEmailLogsForPasses(@RequestBody List<Long> passIds) {
        // We will add security later. For now, it's open for inter-service communication.

        List<EmailAuditLog> logs = emailAuditLogRepository.findByAssociatedPassIdIn(passIds);

        List<EmailAuditLogResponse> response = logs.stream()
                .map(log -> new EmailAuditLogResponse(
                        log.getId(),
                        log.getAssociatedPassId(),
                        log.getRecipientAddress(),
                        log.getSubject(),
                        log.getStatus().name(),
                        log.getCreatedAt(),
                        log.getProcessedAt(),
                        log.getFailureReason()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}