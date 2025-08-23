package com.gt.visitor_pass_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String uniqueId; // Unique, non-editable ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String contact;
    private String role; // e.g., "ROLE_EMPLOYEE", "ROLE_ADMIN"
    private boolean isActive;

    // --- NEW FIELDS ---
    private LocalDate joiningDate;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String gender;
    private String department;
}