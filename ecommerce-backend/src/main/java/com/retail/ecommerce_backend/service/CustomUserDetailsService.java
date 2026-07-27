package com.retail.ecommerce_backend.service;

import com.retail.ecommerce_backend.model.User;
import com.retail.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

//Spring Security a besoin qu'on lui dise 
// comment charger un utilisateur depuis notre base.
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

     private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. On cherche l'utilisateur en BDD par son email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // 2. On transforme notre entité User en un objet UserDetails attendu par Spring Security
        //    On mappe le rôle (ex: "ADMIN") en une autorité de type "ROLE_ADMIN"
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

}
