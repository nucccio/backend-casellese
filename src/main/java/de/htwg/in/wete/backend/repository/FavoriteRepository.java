package de.htwg.in.wete.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.htwg.in.wete.backend.model.Favorite;
import de.htwg.in.wete.backend.model.Recipe;
import de.htwg.in.wete.backend.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    // Alle Favoriten eines Users finden
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
    
    // Alle Favoriten eines Users über oauthId finden
    @Query("SELECT f FROM Favorite f WHERE f.user.oauthId = :oauthId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserOauthIdOrderByCreatedAtDesc(@Param("oauthId") String oauthId);
    
    // Prüfen ob ein Rezept bereits Favorit ist
    Optional<Favorite> findByUserAndRecipe(User user, Recipe recipe);
    
    // Prüfen ob ein Rezept bereits Favorit ist (über oauthId und recipeId)
    @Query("SELECT f FROM Favorite f WHERE f.user.oauthId = :oauthId AND f.recipe.id = :recipeId")
    Optional<Favorite> findByUserOauthIdAndRecipeId(@Param("oauthId") String oauthId, @Param("recipeId") Long recipeId);
    
    // Alle Rezept-IDs eines Users als Favoriten
    @Query("SELECT f.recipe.id FROM Favorite f WHERE f.user.oauthId = :oauthId")
    List<Long> findRecipeIdsByUserOauthId(@Param("oauthId") String oauthId);
    
    // Favorit löschen über oauthId und recipeId
    @Query("DELETE FROM Favorite f WHERE f.user.oauthId = :oauthId AND f.recipe.id = :recipeId")
    void deleteByUserOauthIdAndRecipeId(@Param("oauthId") String oauthId, @Param("recipeId") Long recipeId);
    
    // Anzahl der Favoriten eines Users
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.user.oauthId = :oauthId")
    long countByUserOauthId(@Param("oauthId") String oauthId);
}
