package com.Ecommerce.Notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class ServiceAccountTokenProvider {

    private final RestTemplate restTemplate;

    @Value("${app.auth-service.url:http://auth-service:8080}")
    private String authServiceUrl;

    @Value("${app.service-account.client-id:notification-service}")
    private String clientId;

    @Value("${app.service-account.client-secret:secret123}")
    private String clientSecret;

    private String cachedToken;
    private long tokenExpiryTime = 0;

    public ServiceAccountTokenProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getToken() {
        // If token is expired or not present, fetch a new one
        if (cachedToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            fetchNewToken();
        }
        return cachedToken;
    }

    private void fetchNewToken() {
        try {
            String url = authServiceUrl + "/api/v1/auth/service-account/login";

            Map<String, String> requestBody = Map.of(
                    "clientId", clientId,
                    "clientSecret", clientSecret
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                cachedToken = (String) response.getBody().get("token");
                // Set expiry to 18 minutes (token expires in 20 minutes, refresh 2 minutes before)
                tokenExpiryTime = System.currentTimeMillis() + (18 * 60 * 1000);
                log.info("Successfully fetched new service account token");
            } else {
                log.error("Failed to fetch service account token. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching service account token", e);
        }
    }
}