package de.htwg.in.wete.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.wete.backend.model.Category;
import de.htwg.in.wete.backend.model.Product;
import de.htwg.in.wete.backend.model.Role;
import de.htwg.in.wete.backend.model.User;
import de.htwg.in.wete.backend.repository.ProductRepository;
import de.htwg.in.wete.backend.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Checks if the user identified by the JWT has ADMIN role.
     * @param jwt The JWT token from the authenticated user
     * @return true if the user exists and has ADMIN role, false otherwise
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

    @GetMapping
    public List<Product> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Category category) {
        
        if (name != null && category != null) {
            LOG.info("Searching products by name '{}' and category '{}'", name, category);
            return productRepository.findByTitleContainingIgnoreCaseAndCategory(name, category);
        } else if (name != null) {
            LOG.info("Searching products by name '{}'", name);
            return productRepository.findByTitleContainingIgnoreCase(name);
        } else if (category != null) {
            LOG.info("Filtering products by category '{}'", category);
            return productRepository.findByCategory(category);
        }
        
        return productRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@AuthenticationPrincipal Jwt jwt, 
            @Valid @RequestBody Product product) {
        if (!userFromJwtIsAdmin(jwt)) {
            return ResponseEntity.status(403).build();
        }

        if (product.getId() != null) {
            product.setId(null);
            LOG.warn("Attempted to create a product with an existing ID. ID has been set to null to create a new product.");
        }
        Product newProduct = productRepository.save(product);
        LOG.info("Created new product with id " + newProduct.getId());
        return ResponseEntity.ok(newProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@AuthenticationPrincipal Jwt jwt, 
            @PathVariable Long id, @Valid @RequestBody Product productDetails) {
        if (!userFromJwtIsAdmin(jwt)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Product> opt = productRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Product product = opt.get();
        product.setCategory(productDetails.getCategory());
        product.setDescription(productDetails.getDescription());
        product.setImageUrl(productDetails.getImageUrl());
        product.setImageUrlDetails(productDetails.getImageUrlDetails());
        product.setIngredients(productDetails.getIngredients());
        product.setPrice(productDetails.getPrice());
        product.setTitle(productDetails.getTitle());
        Product updatedProduct = productRepository.save(product);
        LOG.info("Updated product with id " + updatedProduct.getId());
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (!userFromJwtIsAdmin(jwt)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Product> opt = productRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        productRepository.delete(opt.get());
        LOG.info("Deleted product with id " + id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}