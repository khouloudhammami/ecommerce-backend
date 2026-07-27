package com.retail.ecommerce_backend.service;


import com.retail.ecommerce_backend.dto.ProductRequest;
import com.retail.ecommerce_backend.dto.ProductResponse;
import com.retail.ecommerce_backend.model.Inventory;
import com.retail.ecommerce_backend.model.Product;
import com.retail.ecommerce_backend.repository.InventoryRepository; 
import com.retail.ecommerce_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor // Lombok génère un constructeur avec les champs 'final' (Injection par constructeur, meilleure pratique)

public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository; // On l'injecte pour gérer le stock

    // Méthode utilitaire pour mapper Entity -> Response
    private ProductResponse mapToResponse(Product product) {
        // On vérifie que l'inventory n'est pas null (sinon on met 0)
        int stock = (product.getInventory() != null) ? product.getInventory().getQuantity() : 0;
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            stock
        );
    }

    // CREATE
    @Transactional // Exécuté dans une transaction. Si une erreur survient, tout est rollback
    public ProductResponse createProduct(ProductRequest request) {
        // 1. On crée l'entité Product
        Product product = Product.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .build();
        
        // 2. On sauvegarde le product pour avoir un ID (nécessaire pour Inventory)
        Product savedProduct = productRepository.save(product);

        // 3. On crée l'inventaire associé avec stock à 0 par défaut
        Inventory inventory = Inventory.builder()
            .quantity(0)
            .reservedQuantity(0)
            .product(savedProduct) // Lien bidirectionnel
            .build();
        inventoryRepository.save(inventory);

        // 4. On met à jour le product avec son inventory (pour que le cache Hibernate soit cohérent)
        savedProduct.setInventory(inventory);

        // 5. On retourne le DTO
        return mapToResponse(savedProduct);
    }

    // READ - Tous les produits
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::mapToResponse) // Transformation de chaque Product en ProductResponse
            .toList(); // Java 16+ : collecte en List immutable
    }

    // READ - Un seul produit par ID
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé avec l'id : " + id));
        return mapToResponse(product);
    }

    // UPDATE
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé"));

        // On modifie uniquement les champs autorisés
        existingProduct.setName(request.name());
        existingProduct.setDescription(request.description());
        existingProduct.setPrice(request.price());

        // Pas besoin de save() explicitement car on est dans @Transactional, 
        // Hibernate détecte les modifications et fait un flush automatiquement.
        return mapToResponse(existingProduct);
    }

    // DELETE
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé");
        }
        // Cascade : comme on a dit CascadeType.ALL dans Product sur inventory,
        // supprimer le product supprimera aussi l'inventory associé.
        productRepository.deleteById(id);
    }

}
