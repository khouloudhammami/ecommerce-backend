package com.retail.ecommerce_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.retail.ecommerce_backend.model.Inventory;
import java.util.Optional;

//@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

     // 🔒 Cette annotation dit à Hibernate de générer : SELECT ... FOR UPDATE
    // Le 'FOR UPDATE' verrouille la ligne en base de données PostgreSQL.
    @Lock(LockModeType.PESSIMISTIC_WRITE) 
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") Long productId);

}
