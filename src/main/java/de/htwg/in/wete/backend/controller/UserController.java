package de.htwg.in.wete.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.wete.backend.model.Role;
import de.htwg.in.wete.backend.model.User;
import de.htwg.in.wete.backend.repository.UserRepository;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controller für die Admin-Nutzerverwaltung.
 * Nur Admins können alle Nutzer sehen und bearbeiten.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Prüft ob der eingeloggte User ein Admin ist.
     */
    private boolean isAdmin(Jwt jwt) {
        String oauthId = jwt.getSubject();
        Optional<User> userOpt = userRepository.findByOauthId(oauthId);
        return userOpt.map(user -> user.getRole() == Role.ADMIN).orElse(false);
    }

    /**
     * GET /api/users - Alle Nutzer auflisten (nur Admin)
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        LOG.info("getAllUsers called by: {}", jwt.getSubject());
        
        if (!isAdmin(jwt)) {
            LOG.warn("Access denied - user is not admin");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<User> users = userRepository.findAll();
        LOG.info("Returning {} users", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id} - Einzelnen Nutzer anzeigen (nur Admin)
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        
        LOG.info("getUserById called for id: {} by: {}", id, jwt.getSubject());
        
        if (!isAdmin(jwt)) {
            LOG.warn("Access denied - user is not admin");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/users/{id} - Nutzer bearbeiten (nur Admin)
     * Admin kann Name, Email und Rolle ändern.
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        
        LOG.info("updateUser called for id: {} by: {}", id, jwt.getSubject());
        
        if (!isAdmin(jwt)) {
            LOG.warn("Access denied - user is not admin");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return userRepository.findById(id)
                .map(user -> {
                    // Name aktualisieren
                    if (updateRequest.getName() != null) {
                        user.setName(updateRequest.getName());
                    }
                    // Email aktualisieren
                    if (updateRequest.getEmail() != null) {
                        user.setEmail(updateRequest.getEmail());
                    }
                    // Rolle aktualisieren (nur Admin darf das)
                    if (updateRequest.getRole() != null) {
                        user.setRole(updateRequest.getRole());
                    }
                    
                    User updatedUser = userRepository.save(user);
                    LOG.info("User {} updated successfully", id);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DTO für User-Updates durch Admin.
     * Admin kann auch die Rolle ändern.
     */
    public static class UserUpdateRequest {
        private String name;
        private String email;
        private Role role;

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

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }
    }
}