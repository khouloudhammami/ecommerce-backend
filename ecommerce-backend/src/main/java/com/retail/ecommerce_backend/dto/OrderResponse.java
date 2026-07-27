package com.retail.ecommerce_backend.dto;

import com.retail.ecommerce_backend.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    BigDecimal totalAmount,
    OrderStatus status,
    LocalDateTime orderDate,
    List<OrderItemResponse> items 
) {
 // 💡 Le record OrderItem est imbriqué ici
    public record OrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice
    ) {}
}

