package com.retail.ecommerce_backend.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "product")
@Data @NoArgsConstructor @AllArgsConstructor @Builder

public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT") // Pour les descriptions longues
    private String description;

    @Column(nullable = false, precision = 10, scale = 2) // 99999999.99 max
    private BigDecimal price;

    // Relation OneToOne avec Inventory. mappedBy indique que c'est Inventory qui contient la colonne product_id
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;
}
