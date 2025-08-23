package com.gt.visitor_pass_service.service;

import com.gt.visitor_pass_service.config.security.JwtTokenProvider;
import com.gt.visitor_pass_service.exception.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TenantSecurityService {

    private final JwtTokenProvider tokenProvider;

    public TenantSecurityService(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public void checkTenantAccess(String authorizationHeader, Long requiredTenantId) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("Authorization header is missing or invalid.");
        }

        String token = authorizationHeader.substring(7);
        Long tokenTenantId = tokenProvider.getTenantIdFromJWT(token);

        if (tokenTenantId == null || !tokenTenantId.equals(requiredTenantId)) {
            throw new AccessDeniedException("You do not have permission to access resources for this location.");
        }
    }
}