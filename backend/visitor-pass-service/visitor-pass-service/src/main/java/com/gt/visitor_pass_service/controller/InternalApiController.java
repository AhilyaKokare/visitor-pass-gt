package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.VisitorPassResponse;
import com.gt.visitor_pass_service.service.VisitorPassService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@Hidden // Hides this controller from the public Swagger UI documentation
public class InternalApiController {

    private final VisitorPassService visitorPassService;

    public InternalApiController(VisitorPassService visitorPassService) {
        this.visitorPassService = visitorPassService;
    }

    @GetMapping("/passes/{id}")
    public ResponseEntity<VisitorPassResponse> getPassById(@PathVariable Long id) {
        VisitorPassResponse response = visitorPassService.getPassById(id);
        return ResponseEntity.ok(response);
    }
}