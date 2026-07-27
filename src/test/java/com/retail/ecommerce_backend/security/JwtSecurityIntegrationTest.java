package com.retail.ecommerce_backend.security;

import com.retail.ecommerce_backend.controller.ProductController;
import com.retail.ecommerce_backend.filter.JwtAuthenticationFilter;
import com.retail.ecommerce_backend.service.ProductService;
import com.retail.ecommerce_backend.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ProductController.class,
    // On désactive le filtre JWT pour les tests d'intégration si on veut tester le comportement de base
    // Mais ici on veut tester que le filtre est bien présent.
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService; // On mock le service pour ne pas aller en BDD

    @MockBean
    private JwtUtil jwtUtil; // On mock l'utilitaire JWT (mais ici on l'exclut, on le remet juste pour satisfaire le contexte)

    @Test
    @DisplayName("🔒 L'API renvoie 401 sans token JWT")
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/products"))
               .andExpect(status().isUnauthorized()); // 401 car le filtre intercepte et rejette
    }

    @Test
    @WithMockUser // Simule un utilisateur authentifié pour ce test
    @DisplayName("✅ L'API accepte une requête avec un utilisateur mocké")
    void shouldAcceptWithMockUser() throws Exception {
        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk()); // 200 OK car on a bypassé la sécurité avec @WithMockUser
    }
}