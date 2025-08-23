package com.gt.visitor_pass_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RejectPassRequest {
    @NotBlank(message = "Rejection reason cannot be blank")
    private String reason;
}