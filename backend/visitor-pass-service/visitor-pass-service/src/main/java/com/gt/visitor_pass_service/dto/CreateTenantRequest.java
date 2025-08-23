package com.gt.visitor_pass_service.dto;

import lombok.Data;

@Data
public class CreateTenantRequest {
    private String name;
    private String locationDetails;
}
