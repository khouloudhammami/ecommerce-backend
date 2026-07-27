package com.retail.ecommerce_backend.dto;

public record AuthResponse(
    String token,
    String email,
    String role
) {}