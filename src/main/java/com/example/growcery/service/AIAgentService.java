package com.example.growcery.service;

import com.example.growcery.model.CartItem;
import com.example.growcery.model.Order;
import com.example.growcery.model.Product;
import com.example.growcery.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIAgentService {

    private static final Logger logger = LoggerFactory.getLogger(AIAgentService.class);
    
    private final GeminiService geminiService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    
    // Simple in-memory storage for agent memory per user
    private final Map<Long, List<String>> userMemory = new HashMap<>();
    
    public AIAgentService(GeminiService geminiService, 
                         ProductService productService, 
                         CartService cartService, 
                         OrderService orderService) {
        this.geminiService = geminiService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        
        // Log service initialization
        logger.info("AIAgentService initialized with dependencies: GeminiService, ProductService, CartService, OrderService");
    }
    
    public Map<String, Object> processUserRequest(Long userId, String situation) {
        Map<String, Object> response = new HashMap<>();
        List<String> memory = userMemory.computeIfAbsent(userId, k -> new ArrayList<>());
        
        // Step 1: Record user request in memory
        memory.add("User asked for recommendations for: " + situation);
        
        // Step 2: Get available products
        List<Product> availableProducts = productService.getAllProducts().stream()
                .filter(product -> product.getStock() > 0)
                .collect(Collectors.toList());
        
        // Step 3: Get AI recommendations
        List<String> recommendedProducts = geminiService.getRecommendedProductNames(situation, availableProducts);
        memory.add("AI recommended: " + String.join(", ", recommendedProducts));
        
        // Step 4: Check availability of recommended products
        List<Product> foundProducts = new ArrayList<>();
        List<String> unavailableProducts = new ArrayList<>();
        
        for (String productName : recommendedProducts) {
            boolean found = false;
            
            for (Product product : availableProducts) {
                // Do a fuzzy match on product name
                if (product.getName().toLowerCase().contains(productName.toLowerCase()) || 
                    productName.toLowerCase().contains(product.getName().toLowerCase())) {
                    foundProducts.add(product);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                unavailableProducts.add(productName);
            }
        }
        
        memory.add("Found products: " + foundProducts.stream().map(Product::getName).collect(Collectors.joining(", ")));
        
        // Step 5: Get alternatives for unavailable products if needed
        if (!unavailableProducts.isEmpty()) {
            memory.add("Unavailable products: " + String.join(", ", unavailableProducts));
            List<String> alternativeNames = geminiService.getAlternativeRecommendations(unavailableProducts, availableProducts);
            
            for (String alternativeName : alternativeNames) {
                for (Product product : availableProducts) {
                    // Do a fuzzy match on product name
                    if (product.getName().toLowerCase().contains(alternativeName.toLowerCase()) || 
                        alternativeName.toLowerCase().contains(product.getName().toLowerCase())) {
                        if (!foundProducts.contains(product)) {
                            foundProducts.add(product);
                            memory.add("Added alternative product: " + product.getName());
                            break;
                        }
                    }
                }
            }
        }
        
        // Build response
        response.put("situation", situation);
        response.put("recommendedProducts", recommendedProducts);
        response.put("unavailableProducts", unavailableProducts);
        response.put("availableProducts", foundProducts);
        
        return response;
    }
    
    public Order createOrderFromAIRecommendationsWithQuantities(Long userId, List<Product> recommendedProducts, 
                                               Map<Long, Integer> productQuantities, String situation) {
        List<String> memory = userMemory.computeIfAbsent(userId, k -> new ArrayList<>());
        
        // Step 1: Clear existing cart
        cartService.clearCart(userId);
        memory.add("Cleared cart for user");
        
        // Step 2: Add recommended products to cart with specified quantities
        for (Product product : recommendedProducts) {
            // Get quantity from the map, default to 1 if not specified
            int quantity = productQuantities.getOrDefault(product.getId(), 1);
            
            // Make sure the quantity is valid
            if (quantity <= 0) {
                quantity = 1;
            }
            
            // Add to cart with the specified quantity
            cartService.addToCart(userId, product.getId(), quantity);
            memory.add("Added to cart: " + product.getName() + " (Quantity: " + quantity + ")");
            
            // Log the quantity for debugging
            logger.info("Adding product {} to cart with quantity {}", product.getName(), quantity);
        }
        
        try {
            // Step 3: Create order from cart
            Order order = orderService.createOrderFromCart(userId);
            memory.add("Created order #" + order.getId() + " based on AI recommendations for situation: " + situation);
            return order;
        } catch (Exception e) {
            logger.error("Error creating order from AI recommendations", e);
            memory.add("Failed to create order: " + e.getMessage());
            throw e;
        }
    }
    
    public Order createOrderFromAIRecommendations(Long userId, List<Product> recommendedProducts, String situation) {
        // Create a default map with quantity 1 for each product
        Map<Long, Integer> defaultQuantities = recommendedProducts.stream()
            .collect(Collectors.toMap(Product::getId, product -> 1));
        
        return createOrderFromAIRecommendationsWithQuantities(userId, recommendedProducts, defaultQuantities, situation);
    }
    
    public List<String> getUserMemory(Long userId) {
        return userMemory.getOrDefault(userId, new ArrayList<>());
    }
    
    public void clearUserMemory(Long userId) {
        userMemory.remove(userId);
    }
}