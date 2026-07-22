package com.retail.ecommerce_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import com.retail.ecommerce_backend.model.enums.*;

@Entity
@Table(name = "orders") // Order est aussi réservé, on met 'orders'
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime orderDate;

    // Relation ManyToOne : Une commande appartient à un seul User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relation OneToMany : Une commande contient plusieurs lignes de produits
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items;

}
