package com.Ecommerce.Auth.repository;

import com.Ecommerce.Auth.entity.ServiceAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceAccountRepository extends JpaRepository<ServiceAccount, UUID> {
    Optional<ServiceAccount> findByClientId(String clientId);
    boolean existsByClientId(String clientId);
}