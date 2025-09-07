package com.example.growcery.controller;

import com.example.growcery.dto.ProductDTO;
import com.example.growcery.model.Product;
import com.example.growcery.service.ProductDescriptionService;
import com.example.growcery.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin/product-description")
@PreAuthorize("hasRole('ADMIN')")
public class ProductDescriptionController {

    private static final Logger logger = LoggerFactory.getLogger(ProductDescriptionController.class);

    private final ProductDescriptionService productDescriptionService;
    private final ProductService productService;

    public ProductDescriptionController(ProductDescriptionService productDescriptionService,
                                      ProductService productService) {
        this.productDescriptionService = productDescriptionService;
        this.productService = productService;
    }

    @GetMapping
    public String showDescriptionGenerator(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/product-description";
    }

    @GetMapping("/{productId}")
    public String showProductDescriptionForm(@PathVariable Long productId, Model model) {
        Optional<Product> productOptional = productService.getProductById(productId);
        
        if (productOptional.isPresent()) {
            model.addAttribute("product", productOptional.get());
            return "admin/product-description-form";
        }
        
        return "redirect:/admin/products";
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateDescription(
            @RequestParam String productName,
            @RequestParam String category,
            @RequestParam(required = false) String basicDescription) {
        
        logger.info("Generating description for: {}, category: {}", productName, category);
        
        Map<String, String> result = productDescriptionService.generateProductDescription(
                productName, category, basicDescription);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateDescriptionForExisting(@PathVariable Long productId) {
        Optional<Product> productOptional = productService.getProductById(productId);
        
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            logger.info("Generating description for existing product: {}, ID: {}", product.getName(), product.getId());
            
            Map<String, String> result = productDescriptionService.generateDescriptionForExistingProduct(
                    productOptional.get());
            return ResponseEntity.ok(result);
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/update/{productId}")
    public String updateProductDescription(
            @PathVariable Long productId,
            @RequestParam String description,
            @RequestParam(required = false) String keywords,
            RedirectAttributes redirectAttributes) {
        
        logger.info("Updating product description for ID: {}", productId);
        
        Optional<Product> productOptional = productService.getProductById(productId);
        
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            
            // Update description
            if (description != null && !description.trim().isEmpty()) {
                product.setProductDescription(description);
            }
            
            // Update keywords if provided
            if (keywords != null) {
                product.setSeoKeywords(keywords);
            }
            
            // Save the product
            productService.updateProduct(product);
            
            redirectAttributes.addFlashAttribute("success", "Product description updated successfully");
            return "redirect:/admin/products";
        }
        
        redirectAttributes.addFlashAttribute("error", "Failed to update product description");
        return "redirect:/admin/products";
    }
    
    @PostMapping("/create-product")
    public String createProductWithDescription(
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "0.00") String price,
            @RequestParam(defaultValue = "0") String stock,
            RedirectAttributes redirectAttributes) {
        
        logger.info("Creating new product with AI-generated description: {}", name);
        
        try {
            // Create a ProductDTO
            ProductDTO productDTO = new ProductDTO();
            productDTO.setName(name);
            productDTO.setProductDescription(description);
            
            // Parse category
            try {
                productDTO.setCategory(Product.Category.valueOf(category));
            } catch (IllegalArgumentException e) {
                // Default to FRUIT if category is invalid
                productDTO.setCategory(Product.Category.FRUIT);
            }
            
            // Parse price and stock with error handling
            try {
                productDTO.setPrice(new BigDecimal(price));
            } catch (NumberFormatException e) {
                productDTO.setPrice(BigDecimal.ZERO);
            }
            
            try {
                productDTO.setStock(Integer.parseInt(stock));
            } catch (NumberFormatException e) {
                productDTO.setStock(0);
            }
            
            // Set SEO keywords
            productDTO.setSeoKeywords(keywords);
            
            // Save the product
            Product savedProduct = productService.saveProduct(productDTO);
            
            redirectAttributes.addFlashAttribute("success", "Product created successfully with AI-generated description");
            return "redirect:/admin/products";
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to create product: " + e.getMessage());
            return "redirect:/admin/product-description";
        }
    }
}