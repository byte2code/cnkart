package com.cnkart.order.repository;

import com.cnkart.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
    Optional<Order> findByOrderReference(String orderReference);
}
