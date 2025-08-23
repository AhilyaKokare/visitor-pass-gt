package com.gt.visitor_pass_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VisitorPassServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VisitorPassServiceApplication.class, args);
	}

}
