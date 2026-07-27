package com.retail.ecommerce_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// On demande juste l'ID du produit et la quantité voulue

public record OrderItemRequest( 
    @NotNull(message = "L'ID du produit est requis")
    Long productId,
    
    @NotNull(message = "La quantité est requise")
    @Min(value = 1, message = "La quantité doit être au minimum de 1")
    Integer quantity
) {

}
