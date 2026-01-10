package de.htwg.in.wete.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Rezept-Titel darf nicht leer sein")
    @Size(min = 2, max = 200, message = "Rezept-Titel muss zwischen 2 und 200 Zeichen lang sein")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Size(max = 10000, message = "Rezept-Text darf maximal 10000 Zeichen lang sein")
    private String text;

    private String pdfUrl;
    
    private String youtubeUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    // Constructors
    public Recipe() {
    }

    public Recipe(String title, String text, String pdfUrl) {
        this.title = title;
        this.text = text;
        this.pdfUrl = pdfUrl;
    }
    
    public Recipe(String title, String text, String pdfUrl, String youtubeUrl) {
        this.title = title;
        this.text = text;
        this.pdfUrl = pdfUrl;
        this.youtubeUrl = youtubeUrl;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
    
    public String getYoutubeUrl() {
        return youtubeUrl;
    }
    
    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return id != null && id.equals(recipe.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", text='" + (text != null ? text.substring(0, Math.min(50, text.length())) + "..." : "null") + '\'' +
                ", pdfUrl='" + pdfUrl + '\'' +
                ", youtubeUrl='" + youtubeUrl + '\'' +
                '}';
    }
}
