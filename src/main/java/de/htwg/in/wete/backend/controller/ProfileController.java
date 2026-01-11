package de.htwg.in.wete.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import de.htwg.in.wete.backend.model.Role;
import de.htwg.in.wete.backend.model.User;
import de.htwg.in.wete.backend.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        LOGGER.info("getProfile called for principal: {}", oauthId);
        LOGGER.debug("JWT claims: {}", jwt.getClaims());
        
        if (oauthId == null) {
            LOGGER.warn("JWT does not contain 'sub' claim");
            return ResponseEntity.badRequest().build();
        }
        
        // Suche nach existierendem User oder erstelle neuen User mit REGULAR-Rolle
        User user = userRepository.findByOauthId(oauthId)
                .orElseGet(() -> {
                    LOGGER.info("Creating new user with oauthId: {}", oauthId);
                    User newUser = new User();
                    newUser.setOauthId(oauthId);
                    // Name und Email aus JWT-Claims extrahieren
                    newUser.setName(jwt.getClaimAsString("name"));
                    newUser.setEmail(jwt.getClaimAsString("email"));
                    // Neue User bekommen standardmäßig die REGULAR-Rolle
                    newUser.setRole(Role.REGULAR);
                    return userRepository.save(newUser);
                });
        
        return ResponseEntity.ok(user);
    }

    /**
     * Aktualisiert das Profil des eingeloggten Benutzers.
     * Benutzer können ihren Namen und ihre E-Mail ändern.
     * Die Rolle kann nicht vom Benutzer selbst geändert werden.
     */
    @PutMapping
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProfileUpdateRequest updateRequest) {
        
        String oauthId = jwt.getSubject();
        LOGGER.info("updateProfile called for principal: {}", oauthId);
        
        if (oauthId == null) {
            LOGGER.warn("JWT does not contain 'sub' claim");
            return ResponseEntity.badRequest().build();
        }
        
        return userRepository.findByOauthId(oauthId)
                .map(user -> {
                    // Nur Name und Email können geändert werden
                    if (updateRequest.getName() != null) {
                        user.setName(updateRequest.getName());
                    }
                    if (updateRequest.getEmail() != null) {
                        user.setEmail(updateRequest.getEmail());
                    }
                    
                    User updatedUser = userRepository.save(user);
                    LOGGER.info("Profile updated for user with oauthId: {}", oauthId);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DTO für Profil-Updates.
     * Enthält nur die Felder, die der Benutzer ändern darf.
     */
    public static class ProfileUpdateRequest {
        private String name;
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}