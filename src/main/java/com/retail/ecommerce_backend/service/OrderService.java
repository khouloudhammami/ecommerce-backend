package com.retail.ecommerce_backend.service;

import com.retail.ecommerce_backend.dto.OrderItemRequest;
import com.retail.ecommerce_backend.dto.OrderRequest;
import com.retail.ecommerce_backend.dto.OrderResponse;
import com.retail.ecommerce_backend.event.OrderPlacedEvent;
import com.retail.ecommerce_backend.model.*;
import com.retail.ecommerce_backend.model.enums.OrderStatus;
import com.retail.ecommerce_backend.repository.InventoryRepository;
import com.retail.ecommerce_backend.repository.OrderRepository;
import com.retail.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.ApplicationEventPublisher; 
import com.retail.ecommerce_backend.event.OrderPlacedEvent; 


import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j //Lombok crée la variable 'log' automatiquement sous le capot !
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    //Injection du publisher d'événements Spring
    private final ApplicationEventPublisher eventPublisher;
  


    //  Le @Transactional est OBLIGATOIRE ici.
    // Il garantit :
    // 1. Que le verrou FOR UPDATE sera bien maintenu pendant tout le traitement.
    // 2. Que si une exception est levée (ex: stock insuffisant), la transaction est annulée (ROLLBACK) 
    // et la base ne garde aucune trace de la commande ni de la modif du stock.
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 1. On vérifie que l'utilisateur existe (sinon on jette une 404)
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        // 2. On prépare l'entité Order (sans les items pour l'instant)
        Order order = Order.builder()
            .user(user)
            .status(OrderStatus.PENDING) // Toute nouvelle commande commence en "En attente"
            .orderDate(LocalDateTime.now())
            .totalAmount(BigDecimal.ZERO) // On mettra à jour après le calcul
            .build();

        // 3. On initialise la liste des items et le total
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // 4. On boucle sur chaque produit demandé dans le JSON
        for (OrderItemRequest itemReq : request.items()) {

            // 🔒 CRUCIAL : On appelle notre méthode avec verrou !
            // Si deux requêtes arrivent en même temps, la seconde attendra ici.
            Inventory inventory = inventoryRepository.findByProductIdWithLock(itemReq.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Produit avec l'ID " + itemReq.productId() + " non trouvé"));

            // On récupère le produit associé à l'inventaire
            Product product = inventory.getProduct();

            // 5. Validation du stock disponible
            if (inventory.getQuantity() < itemReq.quantity()) {
                // On jette une exception -> Spring fait un ROLLBACK automatiquement
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Stock insuffisant pour le produit : " + product.getName() + 
                    ". Disponible : " + inventory.getQuantity());
            }

            // 6. Mise à jour du stock (on déduit la quantité)
            // Comme on est en @Transactional et qu'on a le verrou, 
            // cet update est parfaitement sécurisé.
            inventory.setQuantity(inventory.getQuantity() - itemReq.quantity());

            // 7. On construit l'OrderItem (ligne de commande)
            OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(itemReq.quantity())
                .price(product.getPrice()) // On fige le prix unitaire du produit dans la ligne
                .order(order) // On attache cette ligne à la commande parente
                .build();

            orderItems.add(orderItem);
            
            // On cumule le total (prix * quantité)
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            total = total.add(itemTotal);
        }

        // 8. On attache les items et le total à la commande
        order.setItems(orderItems);
        order.setTotalAmount(total);

        // 9. Sauvegarde en base (UN SEUL appel grâce à cascade=CascadeType.ALL)
        // Cela va faire un INSERT dans "orders" et plusieurs INSERT dans "order_item".
        Order savedOrder = orderRepository.save(order);

        // 10. 🚀 PUBLICATION DE L'ÉVÉNEMENT (MAINTENANT QUE LA BDD EST COMMITÉE)
        // On crée l'événement avec les données de la commande fraîchement créée
        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getUser().getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getOrderDate()
        );
        // Spring va propager cet événement à tous les beans qui l'écoutent (@EventListener)
        // Comme notre OrderEventListener écoute en phase AFTER_COMMIT, il attendra que 
        // cette méthode @Transactional soit terminée (commit) avant d'envoyer sur Kafka.
        eventPublisher.publishEvent(event);
        log.info("📦 Événement OrderPlacedEvent publié dans Spring pour l'orderId : {}", savedOrder.getId());

        // 11. On retourne la réponse (le frontend n'attend pas que Kafka ait fini)

        return mapToResponse(savedOrder);
    }

    // Méthode utilitaire pour transformer l'Entity Order en OrderResponse
    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemsResponse = order.getItems().stream()
            .map(item -> new OrderResponse.OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice()
            ))
            .toList();

        return new OrderResponse(
            order.getId(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getOrderDate(),
            itemsResponse
        );
    }

}
