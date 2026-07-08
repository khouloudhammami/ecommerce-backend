package com.retail.ecommerce_backend.dto;

import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer stockQuantity // On expose le stock directement
) {

}
