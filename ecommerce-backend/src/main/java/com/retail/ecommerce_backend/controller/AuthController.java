package com.retail.ecommerce_backend.controller;

import com.retail.ecommerce_backend.dto.AuthRequest;
import com.retail.ecommerce_backend.dto.AuthResponse;
import com.retail.ecommerce_backend.model.User;
import com.retail.ecommerce_backend.repository.UserRepository;
import com.retail.ecommerce_backend.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔐 Endpoint de Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        // 1. On demande à Spring Security de vérifier les credentials
        // Si ça échoue, il lève une exception (BadCredentialsException)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Si tout est bon, on charge l'utilisateur
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        // 3. On génère le JWT
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. On récupère le rôle depuis la BDD pour le renvoyer au frontend
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        // 5. On retourne la réponse
        return ResponseEntity.ok(new AuthResponse(jwt, user.getEmail(), user.getRole().name()));
    }

    // (Optionnel) Endpoint d'inscription rapide pour créer des utilisateurs de test
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email déjà utilisé");
        }

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password())) // On hash le mot de passe
            .fullName("Default User")
            .role(com.retail.ecommerce_backend.model.enums.Role.USER) // Par défaut, simple utilisateur
            .build();

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur créé avec succès");
    }
}