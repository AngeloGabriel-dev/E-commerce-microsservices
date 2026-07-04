package com.Ecommerce.Auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "service_accounts")
public class ServiceAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "client_id", nullable = false, unique = true, length = 100)
    private String clientId;

    @Column(name = "client_secret_hash", nullable = false, length = 200)
    private String clientSecretHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "roles", nullable = false, length = 25)
    private Role role = Role.ROLE_SERVICE;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    public enum Role {
        ROLE_ORDER_SERVICE,
        ROLE_PAYMENT_SERVICE,
        ROLE_NOTIFICATION_SERVICE,
        ROLE_CATALOG_SERVICE,
        ROLE_USER_SERVICE,
        ROLE_AUTH_SERVICE,
        ROLE_SERVICE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceAccount that = (ServiceAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ServiceAccount{" +
                "id=" + id +
                ", clientId='" + clientId + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }
}