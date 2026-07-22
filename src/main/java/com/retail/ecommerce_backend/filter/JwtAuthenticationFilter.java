package com.retail.ecommerce_backend.filter;

import com.retail.ecommerce_backend.service.CustomUserDetailsService;
import com.retail.ecommerce_backend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 1. Récupérer le header "Authorization"
        final String authHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;
         // 2. Vérifier que le header est bien présent et commence par "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // On retire "Bearer "
            try {
                // 3. Extraire l'email du token
                email = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // En cas de token invalide (expiré, mal formé), on ne fait rien, 
                // le filtrage continuera mais l'utilisateur ne sera pas authentifié.
                logger.warn("Token JWT invalide : " + e.getMessage());
            }
        }
     // 4. Si on a un email et que personne n'est encore authentifié dans le contexte actuel
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 5. Charger les détails de l'utilisateur depuis la BDD
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

            // 6. Valider le token par rapport à l'utilisateur chargé
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 7. Créer un objet Authentication (contenant les rôles) et le placer dans le contexte
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 8. Passer la main au prochain filtre de la chaîne (le contrôleur sera atteint après)
        chain.doFilter(request, response);
    }

}
