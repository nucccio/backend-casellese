package de.htwg.in.wete.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS-Konfiguration für das Backend.
 * 
 * Wichtig für Auth0:
 * - Authorization Header muss erlaubt sein (für Bearer Token)
 * - Credentials müssen erlaubt sein (für Cookies/Auth)
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Erlaubte Origins (für Entwicklung und Produktion)
                .allowedOrigins(
                    "http://localhost:5173",                    // Vite Dev Server
                    "http://localhost:5174",                    // Vite Dev Server (Alternative Port)
                    "http://localhost:3000",                    // Alternative Dev Server
                    "https://htwg-in-schneider.github.io",      // GitHub Pages Production
                    "https://frontend-casellese.onrender.com"   // Render Production (falls verwendet)
                )
                // Erlaubte HTTP-Methoden
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // Erlaubte Headers (Authorization ist wichtig für JWT!)
                .allowedHeaders("*")
                // Exposed Headers (damit Frontend auf diese zugreifen kann)
                .exposedHeaders("Authorization", "Content-Type")
                // Credentials erlauben (wichtig für Auth)
                .allowCredentials(true)
                // Cache für Preflight-Requests (OPTIONS)
                .maxAge(3600);
    }
}