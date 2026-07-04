package com.Ecommerce.Notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class BearerTokenInterceptor implements ClientHttpRequestInterceptor {

    private final ServiceAccountTokenProvider tokenProvider;

    public BearerTokenInterceptor(ServiceAccountTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String token = tokenProvider.getToken();
        if (token != null && !token.isEmpty()) {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            log.debug("Added Bearer token to request: {}", request.getURI());
        }
        return execution.execute(request, body);
    }
}
