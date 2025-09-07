package com.example.growcery.controller;

import com.example.growcery.model.Product;
import com.example.growcery.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        // Check if user is admin
        boolean isAdmin = authentication != null && 
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        if (isAdmin) {
            return "redirect:/admin/dashboard";
        }
        
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "home";
    }
    
    @GetMapping("/products")
    public String products(@RequestParam(required = false) String category, Model model) {
        List<Product> products;
        
        if (category != null && !category.isEmpty()) {
            try {
                Product.Category productCategory = Product.Category.valueOf(category.toUpperCase());
                products = productService.getProductsByCategory(productCategory);
                model.addAttribute("selectedCategory", productCategory);
            } catch (IllegalArgumentException e) {
                products = productService.getAllProducts();
            }
        } else {
            products = productService.getAllProducts();
        }
        
        model.addAttribute("products", products);
        model.addAttribute("categories", Product.Category.values());
        return "products";
    }
}