package com.Ecommerce.Auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAccountLoginDto {
    @NotBlank(message = "Client ID is required")
    @Size(min = 3, max = 100, message = "Client ID must be between 3 and 100 characters")
    private String clientId;

    @NotBlank(message = "Client Secret is required")
    @Size(min = 3, max = 100, message = "Client Secret must be between 3 and 100 characters")
    private String clientSecret;
}