package com.gt.visitor_pass_service.service;

import com.gt.visitor_pass_service.model.AuditLog;
import com.gt.visitor_pass_service.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// --- THIS IS THE FIX: Import the specific assertion methods you need ---
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull; // <-- THIS LINE FIXES THE ERROR
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void logEvent_shouldCreateAndSaveAuditLogWithCorrectData() {
        // --- ARRANGE ---
        String action = "USER_CREATED";
        Long userId = 101L;
        Long tenantId = 1L;
        Long passId = null; // Can be null for user events

        // --- ACT ---
        auditService.logEvent(action, userId, tenantId, passId);

        // --- ASSERT ---
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();

        assertNotNull(capturedLog);
        assertEquals(action, capturedLog.getActionDescription());
        assertEquals(userId, capturedLog.getUserId());
        assertEquals(tenantId, capturedLog.getTenantId());
        
        // This line will now compile correctly because of the static import
        assertNull(capturedLog.getPassId()); 
        
        assertNotNull(capturedLog.getTimestamp());
    }
}