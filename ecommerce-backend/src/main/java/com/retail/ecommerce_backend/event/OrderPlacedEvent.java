package com.retail.ecommerce_backend.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Ce Record sera automatiquement transformé en JSON par le JsonSerializer de Kafka.
public record OrderPlacedEvent(
    Long orderId,
    Long userId,
    BigDecimal totalAmount,
    LocalDateTime createdAt
) {}
