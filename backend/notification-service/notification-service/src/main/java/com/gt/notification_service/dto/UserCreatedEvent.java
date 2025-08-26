package com.gt.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * Event published to RabbitMQ when a new user is created by a Tenant Admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent implements Serializable {
    private String newUserName;
    private String newUserEmail;
    private String newUserRole;
    private String tenantName;
    private String loginUrl; // URL to the frontend login page
    private String createdByAdminName; // <-- ADD THIS LINE
}