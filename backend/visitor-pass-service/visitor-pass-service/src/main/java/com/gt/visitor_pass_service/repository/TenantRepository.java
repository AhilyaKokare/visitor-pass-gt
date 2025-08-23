package com.gt.visitor_pass_service.repository;

import com.gt.visitor_pass_service.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}