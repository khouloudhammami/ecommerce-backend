package com.retail.ecommerce_backend.repository;

import com.retail.ecommerce_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Optionnel mais explicite. Spring Data JPA fournit déjà l'implémentation
public interface ProductRepository  extends JpaRepository<Product, Long> {
    // Ici on peut ajouter des méthodes personnalisées plus tard (ex: findByPriceBetween)

}
