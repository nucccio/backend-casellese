package de.htwg.in.wete.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.htwg.in.wete.backend.model.Category;
import de.htwg.in.wete.backend.model.Product;
import de.htwg.in.wete.backend.model.Recipe;
import de.htwg.in.wete.backend.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Tests für die Bean Validation der Model-Klassen.
 * Prüft, dass ungültige Daten korrekt abgelehnt werden.
 * 
 * Hinweis: Diese Tests validieren die Model-Annotationen direkt.
 * Die tatsächliche Validierung im Controller wird durch @Valid aktiviert.
 */
class ValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== PRODUCT TESTS ====================

    @Test
    void product_validData_noViolations() {
        Product product = new Product();
        product.setTitle("Ciabatta");
        product.setCategory(Category.BROT);
        product.setPrice(8.50);
        product.setDescription("Italienisches Weißbrot");

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertTrue(violations.isEmpty(), "Gültige Daten sollten keine Fehler erzeugen");
    }

    @Test
    void product_emptyTitle_violation() {
        Product product = new Product();
        product.setTitle("");  // Ungültig: leer
        product.setCategory(Category.BROT);
        product.setPrice(10.0);

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Leerer Titel sollte Fehler erzeugen");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")),
                "Fehler sollte sich auf 'title' beziehen");
    }

    @Test
    void product_nullTitle_violation() {
        Product product = new Product();
        product.setTitle(null);  // Ungültig: null
        product.setCategory(Category.SALAMI);
        product.setPrice(10.0);

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Null-Titel sollte Fehler erzeugen");
    }

    @Test
    void product_titleTooShort_violation() {
        Product product = new Product();
        product.setTitle("A");  // Ungültig: nur 1 Zeichen (min ist 2)
        product.setCategory(Category.KAESE);
        product.setPrice(10.0);

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Zu kurzer Titel sollte Fehler erzeugen");
    }

    @Test
    void product_negativePrice_violation() {
        Product product = new Product();
        product.setTitle("Test Produkt");
        product.setCategory(Category.BROT);
        product.setPrice(-5.0);  // Ungültig: negativ

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Negativer Preis sollte Fehler erzeugen");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("price")),
                "Fehler sollte sich auf 'price' beziehen");
    }

    @Test
    void product_zeroPrice_noViolation() {
        Product product = new Product();
        product.setTitle("Gratis Produkt");
        product.setCategory(Category.SALAMI);
        product.setPrice(0.0);  // Gültig: 0 ist erlaubt

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertTrue(violations.isEmpty(), "Preis 0 sollte erlaubt sein");
    }

    @Test
    void product_nullCategory_violation() {
        Product product = new Product();
        product.setTitle("Test Produkt");
        product.setCategory(null);  // Ungültig: null
        product.setPrice(10.0);

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Null-Kategorie sollte Fehler erzeugen");
    }

    // ==================== RECIPE TESTS ====================

    @Test
    void recipe_validData_noViolations() {
        Recipe recipe = new Recipe();
        recipe.setTitle("Tomatensoße Grundrezept");
        recipe.setText("Tomaten kochen und pürieren...");
        recipe.setPdfUrl("https://example.com/rezept.pdf");
        recipe.setYoutubeUrl("https://youtube.com/watch?v=abc123");

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipe);
        
        assertTrue(violations.isEmpty(), "Gültige Daten sollten keine Fehler erzeugen");
    }

    @Test
    void recipe_emptyTitle_violation() {
        Recipe recipe = new Recipe();
        recipe.setTitle("");  // Ungültig
        recipe.setText("Rezepttext");

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipe);
        
        assertFalse(violations.isEmpty(), "Leerer Rezept-Titel sollte Fehler erzeugen");
    }

    @Test
    void recipe_titleTooShort_violation() {
        Recipe recipe = new Recipe();
        recipe.setTitle("A");  // Ungültig: nur 1 Zeichen

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipe);
        
        assertFalse(violations.isEmpty(), "Zu kurzer Rezept-Titel sollte Fehler erzeugen");
    }

    // ==================== USER TESTS ====================

    @Test
    void user_validData_noViolations() {
        User user = new User();
        user.setOauthId("auth0|123456");
        user.setEmail("test@example.com");
        user.setName("Max Mustermann");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertTrue(violations.isEmpty(), "Gültige Daten sollten keine Fehler erzeugen");
    }

    @Test
    void user_invalidEmail_violation() {
        User user = new User();
        user.setOauthId("auth0|123456");
        user.setEmail("keine-email");  // Ungültig: kein @

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertFalse(violations.isEmpty(), "Ungültige E-Mail sollte Fehler erzeugen");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")),
                "Fehler sollte sich auf 'email' beziehen");
    }

    @Test
    void user_emptyOauthId_violation() {
        User user = new User();
        user.setOauthId("");  // Ungültig: leer
        user.setEmail("test@example.com");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertFalse(violations.isEmpty(), "Leere OAuth-ID sollte Fehler erzeugen");
    }

    @Test
    void user_nullEmail_noViolation() {
        User user = new User();
        user.setOauthId("auth0|123456");
        user.setEmail(null);  // Gültig: Email ist optional

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Email ist optional (kein @NotNull), also keine Violation erwartet
        assertTrue(violations.isEmpty(), "Null-Email sollte erlaubt sein (optional)");
    }
}


// ./mvnw test