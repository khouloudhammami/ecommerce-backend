package com.retail.ecommerce_backend.service;

import com.retail.ecommerce_backend.dto.OrderItemRequest;
import com.retail.ecommerce_backend.dto.OrderRequest;
import com.retail.ecommerce_backend.dto.OrderResponse;
import com.retail.ecommerce_backend.model.*;
import com.retail.ecommerce_backend.model.enums.OrderStatus;
import com.retail.ecommerce_backend.model.enums.Role;
import com.retail.ecommerce_backend.repository.InventoryRepository;
import com.retail.ecommerce_backend.repository.OrderRepository;
import com.retail.ecommerce_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Active Mockito pour cette classe

public class OrderServiceTest {
    @Mock // On crée un faux repository (il ne se connectera pas à la BDD)
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher; // On mock aussi le publisher d'événements

    @InjectMocks // Spring va injecter les mocks ci-dessus dans cette instance de OrderService
    private OrderService orderService;

    private User mockUser;
    private Product mockProduct;
    private Inventory mockInventory;

    @BeforeEach
    void setUp() {
        // 🧑‍💼 Préparation d'un utilisateur factice
        mockUser = User.builder()
            .id(1L)
            .email("client@test.com")
            .fullName("Client Test")
            .role(Role.USER)
            .build();

        // 📦 Préparation d'un produit factice
        mockProduct = Product.builder()
            .id(100L)
            .name("MacBook Pro")
            .price(BigDecimal.valueOf(2499.99))
            .build();

        // 📊 Préparation d'un inventaire (stock = 10)
        mockInventory = Inventory.builder()
            .id(1L)
            .quantity(10)
            .reservedQuantity(0)
            .product(mockProduct)
            .build();
    }

    @Test
    @DisplayName("✅ Création de commande réussie : le stock est déduit")
    void shouldCreateOrderSuccessfullyAndDeductStock() {
        // 1. GIVEN (Préparation)
        // On simule que l'utilisateur existe en BDD
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        // On simule que l'inventaire existe et qu'on le verrouille
        when(inventoryRepository.findByProductIdWithLock(100L)).thenReturn(Optional.of(mockInventory));

        // On simule la sauvegarde de la commande : on retourne une commande avec un ID généré
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderArg = invocation.getArgument(0);
            // On lui assigne un ID factice
            orderArg.setId(999L);
            return orderArg;
        });

        // On crée la requête : achat de 4 MacBooks
        OrderRequest request = new OrderRequest(
            1L,
            List.of(new OrderItemRequest(100L, 4))
        );

        // 2. WHEN (Exécution)
        OrderResponse response = orderService.createOrder(request);

        // 3. THEN (Vérifications)
        // Vérifier que l'inventaire a bien mis à jour son stock (10 -> 6)
        assertThat(mockInventory.getQuantity()).isEqualTo(6); // Le mock a été modifié in-place

        // Vérifier que le total est bien celui attendu (4 * 2499.99 = 9999.96)
        assertThat(response.totalAmount()).isEqualTo(new BigDecimal("9999.96"));
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.items()).hasSize(1);

        // Vérifier que la méthode save du repository a bien été appelée 1 fois
        verify(orderRepository, times(1)).save(any(Order.class));

        // Vérifier que l'événement Kafka a bien été publié (même si c'est mocké)
       verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("❌ Création de commande échoue : stock insuffisant")
    void shouldThrowExceptionWhenStockInsufficient() {
        // 1. GIVEN : Le stock est de 10, mais on demande 15
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(inventoryRepository.findByProductIdWithLock(100L)).thenReturn(Optional.of(mockInventory));

        OrderRequest request = new OrderRequest(
            1L,
            List.of(new OrderItemRequest(100L, 15))
        );

        // 2. WHEN & THEN : On s'attend à ce qu'une exception soit levée
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Stock insuffisant"); // Vérifie le message d'erreur

        // Vérifier que le stock n'a PAS été modifié (il est toujours à 10)
        assertThat(mockInventory.getQuantity()).isEqualTo(10);

        // Vérifier que le repository save N'A PAS été appelé
        verify(orderRepository, never()).save(any());
        
        // Vérifier que l'événement Kafka N'A PAS été publié
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("❌ Création de commande échoue : produit non trouvé")
    void shouldThrowExceptionWhenProductNotFound() {
        // GIVEN : L'utilisateur existe, mais le produit avec l'ID 999 n'existe pas
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(inventoryRepository.findByProductIdWithLock(999L)).thenReturn(Optional.empty());

        OrderRequest request = new OrderRequest(
            1L,
            List.of(new OrderItemRequest(999L, 2))
        );

        // WHEN & THEN : Exception "Produit avec l'ID 999 non trouvé"
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Produit avec l'ID 999 non trouvé");
    }

    @Test
    @DisplayName("❌ Création de commande échoue : utilisateur non trouvé")
    void shouldThrowExceptionWhenUserNotFound() {
        // GIVEN : Aucun utilisateur avec l'ID 999 en BDD
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        OrderRequest request = new OrderRequest(
            999L,
            List.of(new OrderItemRequest(100L, 2))
        );

        // WHEN & THEN
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Utilisateur non trouvé");
    }

}
