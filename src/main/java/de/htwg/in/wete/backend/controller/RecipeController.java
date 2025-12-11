package de.htwg.in.wete.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import de.htwg.in.wete.backend.model.Product;
import de.htwg.in.wete.backend.model.Recipe;
import de.htwg.in.wete.backend.repository.ProductRepository;
import de.htwg.in.wete.backend.repository.RecipeRepository;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;

    public RecipeController(RecipeRepository recipeRepository, ProductRepository productRepository) {
        this.recipeRepository = recipeRepository;
        this.productRepository = productRepository;
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

    // POST new recipe for a product
    @PostMapping("/products/{productId}/recipes")
    public ResponseEntity<Recipe> createRecipe(@PathVariable Long productId, @RequestBody Recipe recipe) {
        return productRepository.findById(productId)
                .map(product -> {
                    recipe.setProduct(product);
                    Recipe savedRecipe = recipeRepository.save(recipe);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT update recipe
    @PutMapping("/recipes/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Long id, @RequestBody Recipe recipeDetails) {
        return recipeRepository.findById(id)
                .map(recipe -> {
                    recipe.setTitle(recipeDetails.getTitle());
                    recipe.setText(recipeDetails.getText());
                    recipe.setPdfUrl(recipeDetails.getPdfUrl());
                    Recipe updatedRecipe = recipeRepository.save(recipe);
                    return ResponseEntity.ok(updatedRecipe);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE recipe
    @DeleteMapping("/recipes/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        return recipeRepository.findById(id)
                .map(recipe -> {
                    recipeRepository.delete(recipe);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
