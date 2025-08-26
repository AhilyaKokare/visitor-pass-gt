package com.gt.visitor_pass_service.service;

import com.gt.visitor_pass_service.dto.CreatePassRequest;
import com.gt.visitor_pass_service.dto.PassApprovedEvent;
import com.gt.visitor_pass_service.dto.VisitorPassResponse;
import com.gt.visitor_pass_service.model.Tenant;
import com.gt.visitor_pass_service.model.User;
import com.gt.visitor_pass_service.model.VisitorPass;
import com.gt.visitor_pass_service.model.enums.PassStatus;
import com.gt.visitor_pass_service.repository.UserRepository;
import com.gt.visitor_pass_service.repository.VisitorPassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitorPassServiceTest {

    @Mock
    private VisitorPassRepository passRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private AuditService auditService;
    @Mock
    private WebSocketUpdateService webSocketUpdateService;

    @InjectMocks
    private VisitorPassService visitorPassService;

    private User employee;
    private User approver;
    private Tenant tenant;
    private VisitorPass pendingPass;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("Main Office");

        employee = new User();
        employee.setId(10L);
        employee.setEmail("employee@example.com");
        employee.setTenant(tenant);

        approver = new User();
        approver.setId(20L);
        approver.setEmail("approver@example.com");

        pendingPass = new VisitorPass();
        pendingPass.setId(100L);
        pendingPass.setStatus(PassStatus.PENDING);
        pendingPass.setCreatedBy(employee);
        pendingPass.setTenant(tenant);
    }

    @Test
    void createPass_shouldSetStatusToPendingAndNotifyWebSocket() {
        // --- ARRANGE ---
        CreatePassRequest request = new CreatePassRequest();
        request.setVisitorName("John Smith");
        // ... set other request properties

        when(userRepository.findByEmail("employee@example.com")).thenReturn(Optional.of(employee));
        // Simulate the save operation by returning the object that was passed in, but with an ID.
        when(passRepository.save(any(VisitorPass.class))).thenAnswer(invocation -> {
            VisitorPass passToSave = invocation.getArgument(0);
            passToSave.setId(100L);
            return passToSave;
        });

        // --- ACT ---
        VisitorPassResponse result = visitorPassService.createPass(1L, request, "employee@example.com");

        // --- ASSERT ---
        ArgumentCaptor<VisitorPass> passCaptor = ArgumentCaptor.forClass(VisitorPass.class);
        verify(passRepository).save(passCaptor.capture());
        VisitorPass savedPass = passCaptor.getValue();

        assertEquals(PassStatus.PENDING, savedPass.getStatus());
        assertEquals("John Smith", savedPass.getVisitorName());
        assertEquals(employee, savedPass.getCreatedBy());

        // Verify that the WebSocket service was notified
        verify(webSocketUpdateService, times(1)).notifyDashboardUpdate(1L);
    }
    
    @Test
    void approvePass_whenPassIsPending_shouldSetStatusToApprovedAndSendEvent() {
        // --- ARRANGE ---
        when(userRepository.findByEmail("approver@example.com")).thenReturn(Optional.of(approver));
        when(passRepository.findById(100L)).thenReturn(Optional.of(pendingPass));
        when(passRepository.save(any(VisitorPass.class))).thenReturn(pendingPass);

        // --- ACT ---
        visitorPassService.approvePass(100L, "approver@example.com");

        // --- ASSERT ---
        ArgumentCaptor<VisitorPass> passCaptor = ArgumentCaptor.forClass(VisitorPass.class);
        verify(passRepository).save(passCaptor.capture());
        VisitorPass savedPass = passCaptor.getValue();

        assertEquals(PassStatus.APPROVED, savedPass.getStatus());
        assertEquals(approver, savedPass.getApprovedBy());

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(PassApprovedEvent.class));
    }
    
    @Test
    void checkIn_whenPassIsNotApproved_shouldThrowIllegalStateException() {
        // --- ARRANGE ---
        // pendingPass has status PENDING, not APPROVED
        when(passRepository.findById(100L)).thenReturn(Optional.of(pendingPass));

        // --- ACT & ASSERT ---
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            visitorPassService.checkIn(100L);
        });
        
        assertEquals("Pass must be approved before check-in.", exception.getMessage());
    }
}