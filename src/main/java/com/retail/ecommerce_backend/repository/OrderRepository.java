package com.retail.ecommerce_backend.repository;

import com.retail.ecommerce_backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
