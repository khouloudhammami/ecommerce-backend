package com.retail.ecommerce_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @NotBlank(message = "L'email est obligatoire")
    String email,
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    String password
) {}