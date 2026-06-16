package com.Ecommerce.Order.repository;

import com.Ecommerce.Order.entity.SellerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SellerOrderRepository extends JpaRepository<SellerOrder, UUID> {

    List<SellerOrder> findByOrderId(UUID orderId);
}