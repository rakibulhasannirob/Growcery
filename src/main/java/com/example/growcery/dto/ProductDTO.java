package com.example.growcery.dto;

import com.example.growcery.model.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String productDescription;

    @NotNull(message = "Category is required")
    private Product.Category category;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be greater than or equal to 0")
    private Integer stock;
    
    private String seoKeywords;
    
    // Default constructor
    public ProductDTO() {
    }
    
    // Full constructor
    public ProductDTO(Long id, String name, String productDescription, Product.Category category, 
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
    
    public Product.Category getCategory() {
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
    
    public void setCategory(Product.Category category) {
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
    
    // Convert from Entity to DTO
    public static ProductDTO fromEntity(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getProductDescription(),
            product.getCategory(),
            product.getPrice(),
            product.getStock(),
            product.getSeoKeywords()
        );
    }
    
    // Convert from DTO to Entity
    public Product toEntity() {
        Product product = new Product();
        product.setId(this.id);
        product.setName(this.name);
        product.setProductDescription(this.productDescription);
        product.setCategory(this.category);
        product.setPrice(this.price);
        product.setStock(this.stock);
        product.setSeoKeywords(this.seoKeywords);
        return product;
    }
}