package com.Ecommerce.Auth.service;

import com.Ecommerce.Auth.dto.ServiceAccountLoginDto;
import com.Ecommerce.Auth.entity.ServiceAccount;
import com.Ecommerce.Auth.exception.PasswordInvalidException;
import com.Ecommerce.Auth.jwt.JwtToken;
import com.Ecommerce.Auth.jwt.JwtUtils;
import com.Ecommerce.Auth.repository.ServiceAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ServiceAccountService {
    private final ServiceAccountRepository serviceAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public JwtToken authenticate(ServiceAccountLoginDto dto) {
        log.info("Processo de autenticação pelo service account {}", dto.getClientId());
        
        ServiceAccount serviceAccount = serviceAccountRepository.findByClientId(dto.getClientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Service Account with clientId = %s not found.", dto.getClientId())
                ));

        if (!serviceAccount.isEnabled()) {
            throw new EntityNotFoundException(
                    String.format("Service Account with clientId = %s is disabled.", dto.getClientId())
            );
        }

        if (!passwordEncoder.matches(dto.getClientSecret(), serviceAccount.getClientSecretHash())) {
            throw new PasswordInvalidException("Invalid client secret");
        }

        return JwtUtils.createToken(serviceAccount.getClientId(), serviceAccount.getRole().name(), serviceAccount.getId());
    }

    @Transactional
    public ServiceAccount createServiceAccount(String clientId, String clientSecret, ServiceAccount.Role role) {
        if (serviceAccountRepository.existsByClientId(clientId)) {
            throw new IllegalArgumentException(
                    String.format("Service Account with clientId = %s already exists.", clientId)
            );
        }

        ServiceAccount serviceAccount = new ServiceAccount();
        serviceAccount.setClientId(clientId);
        serviceAccount.setClientSecretHash(passwordEncoder.encode(clientSecret));
        serviceAccount.setRole(role);
        serviceAccount.setEnabled(true);

        return serviceAccountRepository.save(serviceAccount);
    }

    @Transactional(readOnly = true)
    public ServiceAccount findByClientId(String clientId) {
        return serviceAccountRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Service Account with clientId = %s not found.", clientId)
                ));
    }

    @Transactional(readOnly = true)
    public List<ServiceAccount> findAll() {
        return serviceAccountRepository.findAll();
    }

    @Transactional
    public ServiceAccount updateServiceAccount(UUID id, ServiceAccount.Role role, Boolean enabled) {
        ServiceAccount serviceAccount = serviceAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Service Account with id = %s not found.", id)
                ));

        if (role != null) {
            serviceAccount.setRole(role);
        }
        if (enabled != null) {
            serviceAccount.setEnabled(enabled);
        }

        return serviceAccountRepository.save(serviceAccount);
    }

    @Transactional
    public void deleteServiceAccount(UUID id) {
        ServiceAccount serviceAccount = serviceAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Service Account with id = %s not found.", id)
                ));
        serviceAccountRepository.delete(serviceAccount);
    }
}