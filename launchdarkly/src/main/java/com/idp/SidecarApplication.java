package com.idp.launchdarkly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Spring Boot sidecar application.
 */
@SpringBootApplication
public class SidecarApplication {

    public static void main(String[] args) {
        SpringApplication.run(SidecarApplication.class, args);
    }
}