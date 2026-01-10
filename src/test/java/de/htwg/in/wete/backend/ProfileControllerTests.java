package de.htwg.in.wete.backend;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import de.htwg.in.wete.backend.model.User;
import de.htwg.in.wete.backend.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
public class ProfileControllerTests {

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
    }

    @Test
    public void testGetProfileSuccess() throws Exception {
        // GIVEN: A user exists in the database
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setOauthId("auth0|testuser");
        userRepository.save(user);

        // WHEN: The profile is requested with the corresponding JWT
        mockMvc.perform(get("/api/profile")
                .with(jwt().jwt(jwt -> jwt.claim("sub", "auth0|testuser"))))
                // THEN: Status is OK and user details are returned
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.oauthId").value("auth0|testuser"));
    }

    @Test
    public void testGetProfileCreatesNewUser() throws Exception {
        // GIVEN: No user with oauthId "auth0|unknown" exists

        // WHEN: The profile is requested with a JWT for an unknown user
        mockMvc.perform(get("/api/profile")
                .with(jwt().jwt(jwt -> jwt
                        .claim("sub", "auth0|unknown")
                        .claim("name", "New User")
                        .claim("email", "new@example.com"))))
                // THEN: Status is OK and a new user is created with REGULAR role
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oauthId").value("auth0|unknown"))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.role").value("REGULAR"));
    }

    @Test
    public void testGetProfileUnauthenticated() throws Exception {
        // WHEN: The profile is requested without a JWT
        mockMvc.perform(get("/api/profile"))
                // THEN: Status is Unauthorized
                .andExpect(status().isUnauthorized());
    }
}

// ./mvnw test