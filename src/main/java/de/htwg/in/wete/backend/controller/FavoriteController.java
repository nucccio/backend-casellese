package de.htwg.in.wete.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.wete.backend.model.Favorite;
import de.htwg.in.wete.backend.model.Recipe;
import de.htwg.in.wete.backend.model.Role;
import de.htwg.in.wete.backend.model.User;
import de.htwg.in.wete.backend.repository.FavoriteRepository;
import de.htwg.in.wete.backend.repository.RecipeRepository;
import de.htwg.in.wete.backend.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FavoriteController.class);

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    // ========================================
    // HILFSMETHODEN
    // ========================================

    /**
     * Findet oder erstellt einen User basierend auf JWT.
     * Verhindert doppelte User-Einträge durch Suche nach oauthId UND Email.
     */
    private User findOrCreateUser(Jwt jwt) {
        String oauthId = jwt.getSubject();
        String emailFromJwt = jwt.getClaimAsString("email");
        
        // 1. Zuerst nach oauthId suchen
        return userRepository.findByOauthId(oauthId)
                .orElseGet(() -> {
                    // 2. Falls nicht gefunden, nach Email suchen
                    if (emailFromJwt != null) {
                        return userRepository.findByEmail(emailFromJwt)
                                .map(existingUser -> {
                                    // User per Email gefunden - aktualisiere oauthId
                                    LOGGER.info("Found existing user by email={}, updating oauthId", emailFromJwt);
                                    existingUser.setOauthId(oauthId);
                                    return userRepository.save(existingUser);
                                })
                                .orElseGet(() -> createNewUser(oauthId, jwt));
                    }
                    return createNewUser(oauthId, jwt);
                });
    }

    /**
     * Erstellt einen neuen User mit REGULAR-Rolle.
     */
    private User createNewUser(String oauthId, Jwt jwt) {
        LOGGER.info("Creating new user with oauthId: {}", oauthId);
        User newUser = new User();
        newUser.setOauthId(oauthId);
        newUser.setName(jwt.getClaimAsString("name"));
        newUser.setEmail(jwt.getClaimAsString("email"));
        newUser.setRole(Role.REGULAR);
        return userRepository.save(newUser);
    }

    // ========================================
    // ADMIN-METHODEN
    // ========================================

    /**
     * Prüft ob der eingeloggte User ein Admin ist.
     */
    private boolean isAdmin(Jwt jwt) {
        String oauthId = jwt.getSubject();
        Optional<User> userOpt = userRepository.findByOauthId(oauthId);
        return userOpt.map(user -> user.getRole() == Role.ADMIN).orElse(false);
    }

    /**
     * ADMIN: Alle Favoriten aller User abrufen (Transaktionsdaten einsehen)
     * Erfüllt Anforderung c) "Administratoren können die Transaktionsdaten einsehen"
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<AdminFavoriteDTO>> getAllFavoritesAdmin(@AuthenticationPrincipal Jwt jwt) {
        LOGGER.info("getAllFavoritesAdmin called by: {}", jwt.getSubject());
        
        if (!isAdmin(jwt)) {
            LOGGER.warn("Access denied - user is not admin");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Favorite> favorites = favoriteRepository.findAllByOrderByCreatedAtDesc();
        
        List<AdminFavoriteDTO> favoriteDTOs = favorites.stream()
            .map(this::toAdminDTO)
            .collect(Collectors.toList());
        
        LOGGER.info("Returning {} favorites for admin", favoriteDTOs.size());
        return ResponseEntity.ok(favoriteDTOs);
    }

    /**
     * ADMIN: Statistiken über Favoriten abrufen
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getFavoriteStatsAdmin(@AuthenticationPrincipal Jwt jwt) {
        LOGGER.info("getFavoriteStatsAdmin called by: {}", jwt.getSubject());
        
        if (!isAdmin(jwt)) {
            LOGGER.warn("Access denied - user is not admin");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        long totalFavorites = favoriteRepository.count();
        long totalUsers = userRepository.count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFavorites", totalFavorites);
        stats.put("totalUsers", totalUsers);
        stats.put("averageFavoritesPerUser", totalUsers > 0 ? (double) totalFavorites / totalUsers : 0);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * DTO für Admin-Ansicht mit User-Informationen
     */
    private AdminFavoriteDTO toAdminDTO(Favorite favorite) {
        AdminFavoriteDTO dto = new AdminFavoriteDTO();
        dto.id = favorite.getId();
        dto.recipeId = favorite.getRecipe().getId();
        dto.recipeTitle = favorite.getRecipe().getTitle();
        dto.createdAt = favorite.getCreatedAt().toString();
        
        // User Info für Admin
        if (favorite.getUser() != null) {
            dto.userId = favorite.getUser().getId();
            dto.userName = favorite.getUser().getName();
            dto.userEmail = favorite.getUser().getEmail();
        }
        
        // Product Info falls vorhanden
        if (favorite.getRecipe().getProduct() != null) {
            dto.productId = favorite.getRecipe().getProduct().getId();
            dto.productTitle = favorite.getRecipe().getProduct().getTitle();
        }
        
        return dto;
    }

    /**
     * DTO Klasse für Admin-Ansicht (enthält User-Daten)
     */
    public static class AdminFavoriteDTO {
        public Long id;
        public Long userId;
        public String userName;
        public String userEmail;
        public Long recipeId;
        public String recipeTitle;
        public Long productId;
        public String productTitle;
        public String createdAt;
    }

    // ========================================
    // BESTEHENDE USER-METHODEN (UNVERÄNDERT)
    // ========================================

    /**
     * Alle Favoriten des eingeloggten Users abrufen
     */
    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getFavorites(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        LOGGER.info("getFavorites called for user: {}", oauthId);

        List<Favorite> favorites = favoriteRepository.findByUserOauthIdOrderByCreatedAtDesc(oauthId);
        
        List<FavoriteDTO> favoriteDTOs = favorites.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(favoriteDTOs);
    }

    /**
     * Alle Rezept-IDs abrufen, die der User als Favorit markiert hat
     */
    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getFavoriteIds(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        LOGGER.info("getFavoriteIds called for user: {}", oauthId);

        List<Long> recipeIds = favoriteRepository.findRecipeIdsByUserOauthId(oauthId);
        return ResponseEntity.ok(recipeIds);
    }

    /**
     * Prüfen ob ein bestimmtes Rezept ein Favorit ist
     */
    @GetMapping("/check/{recipeId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recipeId) {
        String oauthId = jwt.getSubject();
        LOGGER.info("checkFavorite called for user: {} and recipe: {}", oauthId, recipeId);

        Optional<Favorite> favorite = favoriteRepository.findByUserOauthIdAndRecipeId(oauthId, recipeId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorite", favorite.isPresent());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Rezept zu Favoriten hinzufügen
     */
    @PostMapping("/{recipeId}")
    @Transactional
    public ResponseEntity<?> addFavorite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recipeId) {
        String oauthId = jwt.getSubject();
        LOGGER.info("addFavorite called for user: {} and recipe: {}", oauthId, recipeId);

        // User finden oder erstellen (mit Duplikat-Schutz)
        User user = findOrCreateUser(jwt);

        // Rezept finden
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
        if (recipeOpt.isEmpty()) {
            LOGGER.warn("Recipe not found: {}", recipeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Rezept nicht gefunden"));
        }

        Recipe recipe = recipeOpt.get();

        // Prüfen ob bereits Favorit
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndRecipe(user, recipe);
        if (existingFavorite.isPresent()) {
            LOGGER.info("Recipe already in favorites");
            return ResponseEntity.ok(toDTO(existingFavorite.get()));
        }

        // Neuen Favoriten erstellen
        Favorite favorite = new Favorite(user, recipe);
        favorite = favoriteRepository.save(favorite);
        
        LOGGER.info("Favorite added successfully: {}", favorite.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(favorite));
    }

    /**
     * Rezept aus Favoriten entfernen
     */
    @DeleteMapping("/{recipeId}")
    @Transactional
    public ResponseEntity<?> removeFavorite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recipeId) {
        String oauthId = jwt.getSubject();
        LOGGER.info("removeFavorite called for user: {} and recipe: {}", oauthId, recipeId);

        Optional<Favorite> favorite = favoriteRepository.findByUserOauthIdAndRecipeId(oauthId, recipeId);
        
        if (favorite.isEmpty()) {
            LOGGER.warn("Favorite not found for user: {} and recipe: {}", oauthId, recipeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Favorit nicht gefunden"));
        }

        favoriteRepository.delete(favorite.get());
        LOGGER.info("Favorite removed successfully");
        
        return ResponseEntity.ok(Map.of("message", "Favorit entfernt"));
    }

    /**
     * Favoriten-Status umschalten (Toggle)
     */
    @PostMapping("/toggle/{recipeId}")
    @Transactional
    public ResponseEntity<?> toggleFavorite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recipeId) {
        String oauthId = jwt.getSubject();
        LOGGER.info("toggleFavorite called for user: {} and recipe: {}", oauthId, recipeId);

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserOauthIdAndRecipeId(oauthId, recipeId);
        
        if (existingFavorite.isPresent()) {
            // Entfernen
            favoriteRepository.delete(existingFavorite.get());
            return ResponseEntity.ok(Map.of(
                "isFavorite", false,
                "message", "Aus Favoriten entfernt"
            ));
        } else {
            // Hinzufügen - User finden oder erstellen (mit Duplikat-Schutz)
            User user = findOrCreateUser(jwt);

            Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
            if (recipeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Rezept nicht gefunden"));
            }

            Favorite favorite = new Favorite(user, recipeOpt.get());
            favoriteRepository.save(favorite);
            
            return ResponseEntity.ok(Map.of(
                "isFavorite", true,
                "message", "Zu Favoriten hinzugefügt"
            ));
        }
    }

    /**
     * Anzahl der Favoriten des Users
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getFavoriteCount(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        long count = favoriteRepository.countByUserOauthId(oauthId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * DTO für Favorite Response (vermeidet Lazy Loading Probleme)
     */
    private FavoriteDTO toDTO(Favorite favorite) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.id = favorite.getId();
        dto.recipeId = favorite.getRecipe().getId();
        dto.recipeTitle = favorite.getRecipe().getTitle();
        dto.recipeText = favorite.getRecipe().getText();
        dto.recipePdfUrl = favorite.getRecipe().getPdfUrl();
        dto.createdAt = favorite.getCreatedAt().toString();
        
        // Product Info falls vorhanden
        if (favorite.getRecipe().getProduct() != null) {
            dto.productId = favorite.getRecipe().getProduct().getId();
            dto.productTitle = favorite.getRecipe().getProduct().getTitle();
            dto.productImageUrl = favorite.getRecipe().getProduct().getImageUrl();
        }
        
        return dto;
    }

    /**
     * DTO Klasse für JSON Response
     */
    public static class FavoriteDTO {
        public Long id;
        public Long recipeId;
        public String recipeTitle;
        public String recipeText;
        public String recipePdfUrl;
        public Long productId;
        public String productTitle;
        public String productImageUrl;
        public String createdAt;
    }
}