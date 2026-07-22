package com.retail.ecommerce_backend.config;

import com.retail.ecommerce_backend.filter.JwtAuthenticationFilter;
import com.retail.ecommerce_backend.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permet d'utiliser @PreAuthorize dans les contrôleurs plus tard
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Provider qui combine notre UserDetailsService et notre PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Le constructeur attend le UserDetailsService en paramètre !
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Manager qui gère l'authentification (utilisé par le controller /login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // La configuration principale des routes et des filtres
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // On désactive CSRF car on est en stateless (JWT)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Les endpoints d'auth sont publics
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger public
                .anyRequest().authenticated() // TOUT le reste est protégé (Product, Orders, etc.)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // On ne crée PAS de session HTTP
            )
            .authenticationProvider(authenticationProvider())
            // On ajoute notre filtre JWT AVANT le filtre UsernamePasswordAuthenticationFilter par défaut
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}