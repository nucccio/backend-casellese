package de.htwg.in.schneider.casellese.backend_casellese.model;

public class Product {
    
    private long id;
    private String title;
    private String description;
    private Category category;
    private double price;
    private String imageUrl;
    private String imageUrl_Details;
    // Delete Ingredients and recipe in Products frontend model

    public long getId() {
        return id;
    }   

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }               

    public void setTitle(String title) {
        this.title = title;
    }   

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl_Details() {
        return imageUrl_Details;
    }

    public void setImageUrl_Details(String imageUrl_Details) {
        this.imageUrl_Details = imageUrl_Details;
    }
}
