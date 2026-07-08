package com.retail.ecommerce_backend.config;

import com.retail.ecommerce_backend.model.Product; 
import com.retail.ecommerce_backend.repository.ProductRepository; 
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner{
    private final ProductRepository productRepository;
    // Injection par constructeur (recommandé)
    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // On vérifie si la table est vide pour ne pas insérer des doublons à chaque démarrage
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

            productRepository.saveAll(List.of(p1, p2, p3));
            System.out.println("✅ 3 produits de test ont été insérés en base de données !");
        }
    }

}
