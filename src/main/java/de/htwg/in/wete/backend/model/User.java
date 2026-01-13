package de.htwg.in.wete.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User Entity für die Benutzerverwaltung.
 * 
 * Jeder User hat eine eindeutige OAuth-ID (von Auth0) und optional eine Email.
 * Die Rolle bestimmt die Berechtigungen (ADMIN oder REGULAR).
 */
@Entity
@Table(name = "app_user", indexes = {
    @Index(name = "idx_user_oauth_id", columnList = "oauthId", unique = true),
    @Index(name = "idx_user_email", columnList = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;
    
    @Size(max = 200, message = "Name darf maximal 200 Zeichen lang sein")
    private String name;
    
    @NotBlank(message = "OAuth-ID darf nicht leer sein")
    @Column(unique = true, nullable = false)
    private String oauthId;

    @Enumerated(EnumType.STRING)
    private Role role = Role.REGULAR;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
