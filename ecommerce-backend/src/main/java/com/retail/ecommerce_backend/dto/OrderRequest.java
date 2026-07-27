package com.retail.ecommerce_backend.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

import jakarta.validation.Valid;

public record OrderRequest(
    @NotNull(message = "L'ID de l'utilisateur est requis")
    Long userId,
    
    @Valid // Permet de valider récursivement chaque élément de la liste
    //Grâce au @Valid, au lieu de juste valider le "contenant" 
    // (la liste en tant que telle), Spring va entrer à l'intérieur de la liste.
    //Il va prendre le premier élément, exécuter les annotations de validation (@NotNull, @Min, etc.) de OrderItemRequest. 
    // Puis il passe au deuxième élément, au troisième, etc.
    List<OrderItemRequest> items
) {
  

}
