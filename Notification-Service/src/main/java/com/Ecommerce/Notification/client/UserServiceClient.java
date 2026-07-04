package com.Ecommerce.Notification.client;

import com.Ecommerce.Notification.dto.UserContactInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    public UserContactInfoDto getUserContactInfo(UUID userId) {
        String url = userServiceUrl + "/api/v1/users/" + userId + "/contact-info";
        log.info("Fetching user contact info from User-Service: {}", url);
        return restTemplate.getForObject(url, UserContactInfoDto.class);
    }
}