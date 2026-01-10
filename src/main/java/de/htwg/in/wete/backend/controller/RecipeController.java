package de.htwg.in.wete.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.wete.backend.model.Recipe;
import de.htwg.in.wete.backend.model.Role;
import de.htwg.in.wete.backend.model.User;
import de.htwg.in.wete.backend.repository.ProductRepository;
import de.htwg.in.wete.backend.repository.RecipeRepository;
import de.htwg.in.wete.backend.repository.UserRepository;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private static final Logger LOG = LoggerFactory.getLogger(RecipeController.class);

    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;

    public RecipeController(RecipeRepository recipeRepository, ProductRepository productRepository) {
        this.recipeRepository = recipeRepository;
        this.productRepository = productRepository;
    }

    /**
     * Checks if the user identified by the JWT has ADMIN role.
     */
    private boolean userFromJwtIsAdmin(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            LOG.warn("JWT or subject is null");
            return false;
        }
        Optional<User> user = userRepository.findByOauthId(jwt.getSubject());
        if (!user.isPresent() || user.get().getRole() != Role.ADMIN) {
            LOG.warn("Unauthorized access by " + user.map(u -> "user with oauthId " + u.getOauthId())
                    .orElse("unknown user"));
            return false;
        }
        return true;
    }

    // GET all recipes
    @GetMapping("/recipes")
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    // GET recipe by id
    @GetMapping("/recipes/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        return recipeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET recipes by product id
    @GetMapping("/products/{productId}/recipes")
    public ResponseEntity<List<Recipe>> getRecipesByProductId(@PathVariable Long productId) {
        if (!productRepository.existsById(productId)) {
            return ResponseEntity.notFound().build();
        }
        List<Recipe> recipes = recipeRepository.findByProductId(productId);
        return ResponseEntity.ok(recipes);
    }

    // POST new recipe for a product (Admin only)
    @PostMapping("/products/{productId}/recipes")
    public ResponseEntity<Recipe> createRecipe(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId, 
            @Valid @RequestBody Recipe recipe) {
        if (!userFromJwtIsAdmin(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return productRepository.findById(productId)
                .map(product -> {
                    recipe.setProduct(product);
                    Recipe savedRecipe = recipeRepository.save(recipe);
                    LOG.info("Created new recipe with id " + savedRecipe.getId());
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT update recipe (Admin only)
    @PutMapping("/recipes/{id}")
    public ResponseEntity<Recipe> updateRecipe(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id, 
            @Valid @RequestBody Recipe recipeDetails) {
        if (!userFromJwtIsAdmin(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return recipeRepository.findById(id)
                .map(recipe -> {
                    recipe.setTitle(recipeDetails.getTitle());
                    recipe.setText(recipeDetails.getText());
                    recipe.setPdfUrl(recipeDetails.getPdfUrl());
                    recipe.setYoutubeUrl(recipeDetails.getYoutubeUrl());
                    Recipe updatedRecipe = recipeRepository.save(recipe);
                    LOG.info("Updated recipe with id " + updatedRecipe.getId());
                    return ResponseEntity.ok(updatedRecipe);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE recipe (Admin only)
    @DeleteMapping("/recipes/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        if (!userFromJwtIsAdmin(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return recipeRepository.findById(id)
                .map(recipe -> {
                    recipeRepository.delete(recipe);
                    LOG.info("Deleted recipe with id " + id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
