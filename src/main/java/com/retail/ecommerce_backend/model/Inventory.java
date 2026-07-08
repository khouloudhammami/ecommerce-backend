package com.retail.ecommerce_backend.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "inventory")
@Data @NoArgsConstructor @AllArgsConstructor @Builder

public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(nullable = false)
    private Integer quantity; // Stock actuel
    
    @Column(nullable = false)
    private Integer reservedQuantity; // Quantité réservée par des commandes en attente de paiement

    // La clé étrangère vers Product (le côté propriétaire de la relation OneToOne)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;



}
