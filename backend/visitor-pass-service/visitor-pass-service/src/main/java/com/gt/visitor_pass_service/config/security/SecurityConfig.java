package com.gt.visitor_pass_service.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true) // Be explicit for clarity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final InternalApiAuthenticationFilter internalApiAuthenticationFilter;

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    public SecurityConfig(
            JwtAuthenticationEntryPoint unauthorizedHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            InternalApiAuthenticationFilter internalApiAuthenticationFilter) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.internalApiAuthenticationFilter = internalApiAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

   // Inside SecurityConfig.java

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // --- PUBLIC ENDPOINTS ---
                    .requestMatchers(SWAGGER_WHITELIST).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    
                    // VVV --- THIS IS THE FIX --- VVV
                    // Allow public access to the WebSocket handshake endpoint
                    .requestMatchers("/ws/**").permitAll()
                    // ^^^ --- END OF FIX --- ^^^

                    // --- INTERNAL SERVICE ENDPOINTS ---
                    .requestMatchers("/api/internal/**").hasAuthority("ROLE_INTERNAL_SERVICE")

                    // --- ROLE-BASED ENDPOINTS ---
                    .requestMatchers("/api/tenants/{tenantId}/passes/**").hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_TENANT_ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/tenants/{tenantId}/approvals").hasAnyAuthority("ROLE_APPROVER", "ROLE_TENANT_ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/tenants/{tenantId}/approvals/**").hasAnyAuthority("ROLE_APPROVER", "ROLE_TENANT_ADMIN")
                    .requestMatchers("/api/tenants/{tenantId}/security/**").hasAnyAuthority("ROLE_SECURITY", "ROLE_TENANT_ADMIN")
                    .requestMatchers("/api/tenants/{tenantId}/admin/**").hasAuthority("ROLE_TENANT_ADMIN")
                    .requestMatchers("/api/super-admin/**").hasAuthority("ROLE_SUPER_ADMIN")
                    
                    .anyRequest().authenticated()
            );

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(internalApiAuthenticationFilter, JwtAuthenticationFilter.class);

    return http.build();
}
}