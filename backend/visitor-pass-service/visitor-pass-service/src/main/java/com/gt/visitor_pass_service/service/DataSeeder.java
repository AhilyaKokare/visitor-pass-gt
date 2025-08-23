package com.gt.visitor_pass_service.service;

import com.gt.visitor_pass_service.model.Tenant;
import com.gt.visitor_pass_service.model.User;
import com.gt.visitor_pass_service.repository.TenantRepository;
import com.gt.visitor_pass_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID; // <-- IMPORT THIS

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(TenantRepository tenantRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if the Super Admin already exists
        if (userRepository.findByEmail("superadmin@system.com").isEmpty()) {
            logger.info("No Super Admin found. Seeding initial data...");

            // 1. Create a global/default tenant for the super admin
            Tenant globalTenant = new Tenant();
            globalTenant.setName("Global Administration");
            globalTenant.setLocationDetails("System-wide access");
            tenantRepository.save(globalTenant);

            // 2. Create the Super Admin user
            User superAdmin = new User();

            // VVV THIS IS THE FIX: SET THE UNIQUE ID FOR THE SUPER ADMIN VVV
            superAdmin.setUniqueId(UUID.randomUUID().toString());

            superAdmin.setName("Super Admin");
            superAdmin.setEmail("superadmin@system.com");
            superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
            superAdmin.setRole("ROLE_SUPER_ADMIN");
            superAdmin.setTenant(globalTenant);
            superAdmin.setActive(true);
            // The other new fields (department, gender, etc.) can be null for the Super Admin

            userRepository.save(superAdmin); // This line was line 47 in your stack trace

            logger.info("************************************************************");
            logger.info("Super Admin created successfully.");
            logger.info("Email: superadmin@system.com | Password: superadmin123");
            logger.info("************************************************************");
        }
    }
}