package com.acme.catchup.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the Catch-Up Platform Spring Boot application.
 * <p>
 * Enables Spring Boot auto-configuration, component scanning, and JPA auditing
 * for automatic entity creation and modification timestamping across domain models.
 * </p>
 *
 * @since 1.0
 */
@EnableJpaAuditing
@SpringBootApplication
public class CatchUpPlatformApplication {

    /**
     * Bootstraps and launches the Spring Boot Catch-Up Platform application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(CatchUpPlatformApplication.class, args);
    }

}
