package com.retail.ecommerce_backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Record Java : constructeur, getters, equals, hashCode prêts à l'emploi
public record ProductRequest( 
    @NotBlank(message = "Le nom est obligatoire") // Validation : ne doit pas être null ou vide
    String name,
    
    String description,
    
    @NotNull(message = "Le prix est obligatoire")
    @Min(value = 0, message = "Le prix ne peut pas être négatif")
    BigDecimal price) {}
