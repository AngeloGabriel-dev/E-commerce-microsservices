package com.Ecommerce.Order.repository;

import com.Ecommerce.Order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByClientIdOrderByCreatedAtDesc(UUID clientId, Pageable pageable);
}