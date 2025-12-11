package de.htwg.in.wete.backend;

import de.htwg.in.wete.backend.model.Category;
import de.htwg.in.wete.backend.model.Product;
import de.htwg.in.wete.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("local") // Use H2 in-memory database for tests
class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testSaveProduct() {
        Product product = new Product();
        product.setTitle("Test Violin");
        product.setDescription("A test violin for unit testing");
        product.setCategory(Category.VIOLIN);
        product.setPrice(999.99);
        product.setImageUrl("https://example.com/violin.jpg");

        Product savedProduct = productRepository.save(product);

        assertNotNull(savedProduct.getId());
        assertEquals("Test Violin", savedProduct.getTitle());
        assertEquals(Category.VIOLIN, savedProduct.getCategory());
    }

    @Test
    void testFindById() {
        Product product = new Product();
        product.setTitle("Find Me Cello");
        product.setDescription("A cello to be found");
        product.setCategory(Category.CELLO);
        product.setPrice(2500.00);
        product.setImageUrl("https://example.com/cello.jpg");

        Product savedProduct = productRepository.save(product);

        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        assertTrue(foundProduct.isPresent());
        assertEquals("Find Me Cello", foundProduct.get().getTitle());
    }

    @Test
    void testFindAll() {
        Product product1 = new Product();
        product1.setTitle("Violin 1");
        product1.setCategory(Category.VIOLIN);
        product1.setPrice(1000.00);

        Product product2 = new Product();
        product2.setTitle("Double Bass 1");
        product2.setCategory(Category.DOUBLE_BASS);
        product2.setPrice(3000.00);

        productRepository.save(product1);
        productRepository.save(product2);

        List<Product> allProducts = productRepository.findAll();

        assertTrue(allProducts.size() >= 2);
    }

    @Test
    void testDeleteProduct() {
        Product product = new Product();
        product.setTitle("Delete Me");
        product.setCategory(Category.ACCESSORIES);
        product.setPrice(50.00);

        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();

        productRepository.deleteById(productId);

        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertFalse(deletedProduct.isPresent());
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product();
        product.setTitle("Original Title");
        product.setCategory(Category.VIOLIN);
        product.setPrice(1000.00);

        Product savedProduct = productRepository.save(product);
        savedProduct.setTitle("Updated Title");
        savedProduct.setPrice(1500.00);

        Product updatedProduct = productRepository.save(savedProduct);

        assertEquals("Updated Title", updatedProduct.getTitle());
        assertEquals(1500.00, updatedProduct.getPrice());
    }
}
