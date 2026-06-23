package com.Ecommerce.Payment.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.mercadopago.MercadoPagoConfig;

@Slf4j
@Configuration
public class MercadoPagoConfiguration {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("Mercado Pago SDK initialized successfully");
    }
}