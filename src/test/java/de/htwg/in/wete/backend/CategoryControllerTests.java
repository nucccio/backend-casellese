package de.htwg.in.wete.backend;

// import de.htwg.in.wete.backend.controller.CategoryController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCategories_shouldReturnAllCategoriesWithGermanNames() throws Exception {
        mockMvc.perform(get("/api/category"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.name == 'BROT')].germanName", contains("Brot")))
                .andExpect(jsonPath("$[?(@.name == 'SALAMI')].germanName", contains("Salami")))
                .andExpect(jsonPath("$[?(@.name == 'KAESE')].germanName", contains("Käse")));
    }

    @Test
    void getCategories_shouldReturnCorrectStructure() throws Exception {
        mockMvc.perform(get("/api/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].germanName").exists());
    }
}

// ./mvnw test