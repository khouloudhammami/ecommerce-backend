package com.retail.ecommerce_backend.model;

//Jakarta.persistance = (JPA) : C'est le catalogue officiel des règles, des concepts et des annotations 
// (@Entity, @Table, @Id). Il dit comment les choses doivent être déclarées.
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import com.retail.ecommerce_backend.model.enums.*;


@Entity
@Table(name = "app_user") // 'user' est un mot réservé en PostgreSQL, on renomme.
@Data // Génère getters, setters, toString, equals, hashCode
@NoArgsConstructor // Constructeur vide (obligatoire pour JPA)
@AllArgsConstructor // Constructeur avec tous les champs
@Builder // Pattern Builder pour créer des objets facilement (ex: User.builder().email("x").build())

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrément PostgreSQL
    private Long id;

    @Column(nullable = false, unique = true) // NOT NULL et UNIQUE en base
    private String email;

    @Column(nullable = false)
    private String password; // Stocké encodé (BCrypt) plus tard

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING) // Stocke "ADMIN" et non l'index 0
    @Column(nullable = false)
    private Role role;

    // Audit : date de création (non modifiable)
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Audit : date de dernière modification
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relation OneToMany avec les commandes (mappedBy signifie que c'est l'entité Order qui possède la clé étrangère)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

}
