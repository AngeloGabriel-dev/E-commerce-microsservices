package com.Ecommerce.Auth.config;

import com.Ecommerce.Auth.entity.ServiceAccount;
import com.Ecommerce.Auth.service.ServiceAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceAccountInitializer implements CommandLineRunner {
    private final ServiceAccountService serviceAccountService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing service accounts...");
        
        // Create default service accounts if they don't exist
        createServiceAccountIfNotExists("order-service", "secret123", ServiceAccount.Role.ROLE_ORDER_SERVICE);
        createServiceAccountIfNotExists("payment-service", "secret123", ServiceAccount.Role.ROLE_PAYMENT_SERVICE);
        createServiceAccountIfNotExists("notification-service", "secret123", ServiceAccount.Role.ROLE_NOTIFICATION_SERVICE);
        createServiceAccountIfNotExists("catalog-service", "secret123", ServiceAccount.Role.ROLE_CATALOG_SERVICE);
        
        log.info("Service accounts initialized successfully!");
    }

    private void createServiceAccountIfNotExists(String clientId, String clientSecret, ServiceAccount.Role role) {
        try {
            serviceAccountService.createServiceAccount(clientId, clientSecret, role);
            log.info("Created service account: {}", clientId);
        } catch (IllegalArgumentException e) {
            log.info("Service account already exists: {}", clientId);
        } catch (Exception e) {
            log.error("Failed to create service account: {}", clientId, e);
        }
    }
}
