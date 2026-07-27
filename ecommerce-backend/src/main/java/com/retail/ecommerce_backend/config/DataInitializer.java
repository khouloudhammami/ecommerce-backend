package com.retail.ecommerce_backend.config;

import com.retail.ecommerce_backend.model.Inventory;
import com.retail.ecommerce_backend.model.Product;
import com.retail.ecommerce_backend.model.User;
import com.retail.ecommerce_backend.model.enums.Role;
import com.retail.ecommerce_backend.repository.InventoryRepository;
import com.retail.ecommerce_backend.repository.ProductRepository;
import com.retail.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor // 💡 Plus besoin de constructeur manuel, Lombok injecte les 3 repositories !
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Initialisation de l'utilisateur par défaut
        if (userRepository.count() == 0) {
            System.out.println("👤 Création de l'utilisateur de test...");
            User user = User.builder()
                .fullName("Khouloud HAMMAMI")
                .email("khouloud@example.com")
                .password("123456")
                .role(Role.USER)
                .build();
            
            userRepository.save(user);
            System.out.println("✅ Utilisateur créé !");
        }

        // 2. Initialisation des produits ET de leur inventaire associé
        if (productRepository.count() == 0) {
            System.out.println("🚀 Initialisation des données de test pour les produits...");

            Product p1 = new Product();
            p1.setName("MacBook Pro 14");
            p1.setDescription("Puce M3, 16 Go RAM, 512 Go SSD");
            p1.setPrice(new BigDecimal("1999.99"));

            Product p2 = new Product();
            p2.setName("iPhone 15 Pro");
            p2.setDescription("Titane naturel, 128 Go");
            p2.setPrice(new BigDecimal("1229.00"));

            Product p3 = new Product();
            p3.setName("Écran Dell 27\"");
            p3.setDescription("Résolution 4K, idéal pour le développement");
            p3.setPrice(new BigDecimal("349.50"));

            // On sauvegarde d'abord les produits pour qu'ils aient un ID généré par la base
            productRepository.saveAll(List.of(p1, p2, p3));
            System.out.println("✅ 3 produits de test ont été insérés !");

          
           // 💡 3. On crée le stock (Inventory) pour chaque produit avec reservedQuantity à 0
    System.out.println("📦 Initialisation des stocks dans l'inventaire...");

    Inventory i1 = new Inventory();
    i1.setProduct(p1);
    i1.setQuantity(10); 


    Inventory i2 = new Inventory();
    i2.setProduct(p2);
    i2.setQuantity(25); 
    i2.setReservedQuantity(0); // 💡 Ajouté !

    Inventory i3 = new Inventory();
    i3.setProduct(p3);
    i3.setQuantity(50); 
    i3.setReservedQuantity(0); // 💡 Ajouté !

    inventoryRepository.saveAll(List.of(i1, i2, i3));
            }
        }
    }