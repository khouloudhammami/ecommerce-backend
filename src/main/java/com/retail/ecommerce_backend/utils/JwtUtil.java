package com.retail.ecommerce_backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component

public class JwtUtil {
     // On injecte les valeurs depuis application.yml (qu'on va ajouter juste après)
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Méthode pour récupérer la clé de signature (HMAC-SHA256)
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extraire le nom d'utilisateur (email) du token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraire la date d'expiration
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraire un claim spécifique (générique)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parser le token et récupérer tous les claims (payload)
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // On utilise la clé pour vérifier la signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //  Vérifier si le token est expiré
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Générer un token pour un utilisateur
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // On peut ajouter le rôle dans les claims pour éviter de re-requêter la BDD à chaque fois
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    // Construction du token
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // Les données personnalisées (rôles)
                .subject(subject) // L'email de l'utilisateur (sub)
                .issuedAt(new Date(System.currentTimeMillis())) // Date de création (iat)
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Expiration (exp)
                .signWith(getSigningKey()) // Signature avec la clé secrète
                .compact();
    }

    // ✅ Valider le token (vérifier l'utilisateur et l'expiration)
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}
