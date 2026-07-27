package com.retail.ecommerce_backend.repository;


import com.retail.ecommerce_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //Méthode dérivée : Spring Data JPA génère automatiquement la requête SQL
    Optional<User> findByEmail(String email); 
    //Optional est un objet qui représente une valeur présente ou absente, 
    // afin d'éviter les erreurs NullPointerException.

}
