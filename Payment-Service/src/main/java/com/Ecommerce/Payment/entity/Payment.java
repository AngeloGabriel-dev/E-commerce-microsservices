package com.Ecommerce.Payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID clientId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false, length = 20)
    private String paymentMethod;

    @Column(unique = true)
    private Long mpPaymentId;

    @Column(length = 255)
    private String mpPreferenceId;

    @Column(length = 30)
    private String mpStatus;

    @Column(length = 255)
    private String mpInitPoint;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PaymentStatus {
        PENDING,
        CONFIRMED,
        FAILED,
        REFUNDED
    }
}