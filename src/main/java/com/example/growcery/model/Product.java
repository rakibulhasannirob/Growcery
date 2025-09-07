package com.example.growcery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be greater than or equal to 0")
    private Integer stock;
    
    @Column(name = "seo_keywords")
    private String seoKeywords;
    
    // Default constructor
    public Product() {
    }
    
    // Full constructor
    public Product(Long id, String name, String productDescription, Category category, 
                  BigDecimal price, Integer stock, String seoKeywords) {
        this.id = id;
        this.name = name;
        this.productDescription = productDescription;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.seoKeywords = seoKeywords;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getProductDescription() {
        return productDescription;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public String getSeoKeywords() {
        return seoKeywords;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public void setSeoKeywords(String seoKeywords) {
        this.seoKeywords = seoKeywords;
    }
    
    // Equals, HashCode, and ToString methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Product product = (Product) o;
        
        return id != null ? id.equals(product.id) : product.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
    
    public enum Category {
        FRUIT, VEGETABLE
    }
}