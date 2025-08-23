package com.gt.visitor_pass_service.model;

import com.gt.visitor_pass_service.model.enums.PassStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "visitor_passes")
public class VisitorPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String visitorName;

    @Column(nullable = false)
    private String visitorEmail; // <-- ADDED THIS LINE

    @Column(nullable = false)
    private String visitorPhone;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private LocalDateTime visitDateTime;

    @Column(unique = true)
    private String passCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PassStatus status; // It's better to use the PassStatus enum directly

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private String rejectionReason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}