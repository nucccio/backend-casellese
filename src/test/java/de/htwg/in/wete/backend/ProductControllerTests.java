package de.htwg.in.wete.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.htwg.in.wete.backend.model.Category;
import de.htwg.in.wete.backend.model.Product;
import de.htwg.in.wete.backend.model.Role;
import de.htwg.in.wete.backend.model.User;
import de.htwg.in.wete.backend.repository.ProductRepository;
import de.htwg.in.wete.backend.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class ProductControllerTests {

    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        productRepository.deleteAll();
        userRepository.deleteAll();
        
        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setOauthId("auth0|admin");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);
    }

    @Test
    void testGetProducts() throws Exception {
        Product product = new Product();
        product.setTitle("Test Caciocavallo");
        product.setCategory(Category.KAESE);
        product.setPrice(12.99);
        productRepository.save(product);

        mockMvc.perform(get("/api/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Caciocavallo"));
    }

    @Test
    void testGetProductById() throws Exception {
        Product product = new Product();
        product.setTitle("Salsiccia Test");
        product.setCategory(Category.SALAMI);
        product.setPrice(15.99);
        product = productRepository.save(product);

        mockMvc.perform(get("/api/product/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Salsiccia Test"));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        mockMvc.perform(get("/api/product/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetProductsByName() throws Exception {
        Product p1 = new Product();
        p1.setTitle("Caciocavallo Silano");
        p1.setCategory(Category.KAESE);
        p1.setPrice(12.99);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setTitle("Mozzarella");
        p2.setCategory(Category.KAESE);
        p2.setPrice(8.99);
        productRepository.save(p2);

        mockMvc.perform(get("/api/product").param("name", "Cacio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Caciocavallo Silano"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetProductsByCategory() throws Exception {
        Product p1 = new Product();
        p1.setTitle("Käse 1");
        p1.setCategory(Category.KAESE);
        p1.setPrice(10.0);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setTitle("Brot 1");
        p2.setCategory(Category.BROT);
        p2.setPrice(5.0);
        productRepository.save(p2);

        mockMvc.perform(get("/api/product").param("category", "KAESE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Käse 1"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testCreateProduct() throws Exception {
        String productPayload = "{\"title\":\"Neuer Käse\",\"category\":\"KAESE\",\"price\":9.99}";

        MvcResult mvcResult = mockMvc.perform(post("/api/product")
                .with(jwt().jwt(jwt -> jwt.claim("sub", "auth0|admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(productPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Neuer Käse"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseContent);
        Long id = json.get("id").asLong();
        assertNotNull(id);

        Product saved = productRepository.findById(id).orElseThrow();
        assertEquals("Neuer Käse", saved.getTitle());
    }

    @Test
    void testUpdateProduct() throws Exception {
        Product existingProduct = new Product();
        existingProduct.setTitle("Original Title");
        existingProduct.setCategory(Category.KAESE);
        existingProduct.setPrice(7.99);
        Long id = productRepository.save(existingProduct).getId();

        String updatePayload = "{\"title\":\"Updated Title\",\"category\":\"SALAMI\",\"price\":19.99}";
        
        mockMvc.perform(put("/api/product/" + id)
                .with(jwt().jwt(jwt -> jwt.claim("sub", "auth0|admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        Product updated = productRepository.findById(id).orElseThrow();
        assertEquals("Updated Title", updated.getTitle());
    }

    @Test
    void testDeleteProduct() throws Exception {
        Product product = new Product();
        product.setTitle("To Be Deleted");
        product.setCategory(Category.BROT);
        product.setPrice(4.99);
        product = productRepository.save(product);

        mockMvc.perform(delete("/api/product/" + product.getId())
                .with(jwt().jwt(jwt -> jwt.claim("sub", "auth0|admin"))))
                .andExpect(status().isNoContent());

        Optional<Product> deleted = productRepository.findById(product.getId());
        assertFalse(deleted.isPresent());
    }
}
    
// ./mvnw test