package de.htwg.in.wete.backend.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Konfiguration für Auth0 JWT-Authentifizierung.
 * 
 * Diese Konfiguration:
 * 1. Aktiviert CORS (Cross-Origin Resource Sharing)
 * 2. Definiert welche Endpoints geschützt sind
 * 3. Konfiguriert JWT-Validierung mit Auth0
 * 
 * Die JWT-Tokens werden automatisch gegen den Auth0-Server validiert.
 * Die Konfiguration dafür erfolgt in application.properties:
 *   - okta.oauth2.issuer
 *   - okta.oauth2.audience
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CORS aktivieren (Konfiguration in WebConfig.java)
                .cors(withDefaults())
                
                // Autorisierungsregeln definieren
                .authorizeHttpRequests((authorize) -> authorize
                        // ========================================
                        // GESCHÜTZTE ENDPOINTS (erfordern JWT)
                        // ========================================
                        
                        // Profil-Endpoint erfordert Authentifizierung
                        .requestMatchers("/api/profile").authenticated()
                        
                        // Produkt-Schreiboperationen erfordern Authentifizierung
                        .requestMatchers(HttpMethod.POST, "/api/product", "/api/product/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/product/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/product/*").authenticated()
                        
                        // Rezept-Schreiboperationen erfordern Authentifizierung
                        .requestMatchers(HttpMethod.POST, "/api/recipe", "/api/recipe/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/recipe/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/recipe/*").authenticated()
                        
                        // ========================================
                        // ÖFFENTLICHE ENDPOINTS (kein JWT nötig)
                        // ========================================
                        
                        // Produkt-Leseoperationen sind öffentlich
                        .requestMatchers(HttpMethod.GET, "/api/product", "/api/product/*").permitAll()
                        
                        // Rezept-Leseoperationen sind öffentlich
                        .requestMatchers(HttpMethod.GET, "/api/recipe", "/api/recipe/*").permitAll()
                        
                        // Kategorie-Endpoints sind öffentlich
                        .requestMatchers("/api/category", "/api/category/*").permitAll()
                        
                        // Alle anderen API-Endpoints sind öffentlich
                        .requestMatchers("/api/**").permitAll()
                        
                        // H2-Console für Entwicklung (nur mit local-Profil aktiv)
                        .requestMatchers("/h2-console/**").permitAll()
                )
                
                // JWT Resource Server konfigurieren
                // Die Validierung erfolgt automatisch gegen den Auth0 Issuer
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(withDefaults()))
                
                .build();
    }
}
