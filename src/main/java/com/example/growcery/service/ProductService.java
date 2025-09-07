package com.example.growcery.service;

import com.example.growcery.dto.ProductDTO;
import com.example.growcery.model.Product;
import com.example.growcery.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(Product.Category category) {
        return productRepository.findByCategory(category);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(ProductDTO productDTO) {
        // Convert DTO to Entity
        Product product = new Product();
        if (productDTO.getId() != null) {
            product.setId(productDTO.getId());
        }
        product.setName(productDTO.getName());
        
        // Handle null values safely
        if (productDTO.getProductDescription() != null) {
            product.setProductDescription(productDTO.getProductDescription());
        }
        
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        
        // Set SEO keywords if provided
        if (productDTO.getSeoKeywords() != null) {
            product.setSeoKeywords(productDTO.getSeoKeywords());
        }
        
        return productRepository.save(product);
    }
    
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public boolean updateStock(Long productId, Integer quantity) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (product.getStock() >= quantity) {
                product.setStock(product.getStock() - quantity);
                productRepository.save(product);
                return true;
            }
        }
        return false;
    }

    public boolean checkStock(Long productId, Integer quantity) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        return optionalProduct.map(product -> product.getStock() >= quantity).orElse(false);
    }
}