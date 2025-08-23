package com.gt.visitor_pass_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime; // <-- Add this import

@Data
@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String locationDetails;

    // --- NEW FIELDS for Auditing and Info ---
    private String createdBy; // Name of the Super Admin who created it
    private LocalDateTime createdAt;
}