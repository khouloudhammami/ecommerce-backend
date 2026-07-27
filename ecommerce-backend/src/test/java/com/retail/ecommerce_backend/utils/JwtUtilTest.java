package com.retail.ecommerce_backend.utils;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    
    // 🔧 Utilisateur factice pour les tests
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // 1. Initialisation de l'objet à tester
        jwtUtil = new JwtUtil();
        
        // 2. Injection des propriétés (comme si elles venaient d'application.yml)
        // On utilise ReflectionTestUtils pour setter les champs privés sans avoir besoin de Spring.
        ReflectionTestUtils.setField(jwtUtil, "secret", "9a4f2c8d3e1b7a6f5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24h
        
        // 3. Création d'un UserDetails factice (Spring Security)
        userDetails = new User(
            "test@retail.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("✅ Génération d'un token valide et extraction de l'email")
    void shouldGenerateValidTokenAndExtractUsername() {
        // GIVEN : L'utilisateur est défini dans le setUp.

        // WHEN : On génère un token
        String token = jwtUtil.generateToken(userDetails);

        // THEN : Le token n'est pas null, et l'email extrait correspond
        assertThat(token).isNotNull().isNotEmpty();
        String extractedEmail = jwtUtil.extractUsername(token);
        assertThat(extractedEmail).isEqualTo("test@retail.com");
    }

    @Test
    @DisplayName("✅ Validation d'un token correct (doit retourner true)")
    void shouldValidateCorrectToken() {
        // GIVEN : Token généré pour l'utilisateur
        String token = jwtUtil.generateToken(userDetails);

        // WHEN : On valide le token AVEC les mêmes UserDetails
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // THEN : La validation est vraie
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("❌ Validation échoue si le token est associé à un autre utilisateur")
    void shouldFailValidationForDifferentUser() {
        // GIVEN : Token généré pour test@retail.com
        String token = jwtUtil.generateToken(userDetails);
        
        // Un autre utilisateur (email différent)
        UserDetails otherUser = new User("hacker@retail.com", "password", Collections.emptyList());

        // WHEN : On valide le token avec un autre utilisateur
        Boolean isValid = jwtUtil.validateToken(token, otherUser);

        // THEN : La validation échoue
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("❌ La méthode isTokenExpired détecte correctement l'expiration")
    void shouldDetectExpiredToken() throws InterruptedException {
        // GIVEN : On crée un token avec une expiration très courte (1ms)
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1 milliseconde
        String token = jwtUtil.generateToken(userDetails); // 👈 La variable s'appelle "token"
        
        // On attend 10ms pour être sûr que le token expire
        Thread.sleep(10);

        // WHEN & THEN : Valider un token expiré doit lever ExpiredJwtException
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            jwtUtil.validateToken(token, userDetails); // 👈 Utilisation de "token"
        }).isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        
        // On remet l'expiration à 24h pour la suite
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }
}