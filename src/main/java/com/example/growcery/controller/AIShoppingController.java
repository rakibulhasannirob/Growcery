package com.example.growcery.controller;

import com.example.growcery.model.Order;
import com.example.growcery.model.Product;
import com.example.growcery.model.User;
import com.example.growcery.service.AIAgentService;
import com.example.growcery.service.ProductService;
import com.example.growcery.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ai-shopping")
public class AIShoppingController {

    private static final Logger logger = LoggerFactory.getLogger(AIShoppingController.class);
    private final AIAgentService aiAgentService;
    private final UserService userService;
    private final ProductService productService;

    public AIShoppingController(AIAgentService aiAgentService, UserService userService, ProductService productService) {
        this.aiAgentService = aiAgentService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping
    public String showAIShoppingPage(Model model, Authentication authentication) {
        // Add any initial data to the model
        model.addAttribute("aiSituation", "");
        return "ai-shopping";
    }

    @PostMapping("/recommend")
    public String getRecommendations(@RequestParam String situation, 
                                    Model model, 
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Map<String, Object> recommendations = aiAgentService.processUserRequest(user.getId(), situation);
            
            model.addAttribute("aiSituation", situation);
            model.addAttribute("recommendations", recommendations);
            model.addAttribute("memory", aiAgentService.getUserMemory(user.getId()));
            
            return "ai-shopping";
        }
        
        redirectAttributes.addFlashAttribute("error", "User not found");
        return "redirect:/";
    }

    @PostMapping("/create-order")
    public String createOrder(@RequestParam String situation,
                             @RequestParam(required = false) List<Long> productIds,
                             @RequestParam Map<String, String> allParams,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent() && productIds != null && !productIds.isEmpty()) {
            User user = userOptional.get();
            
            // Create a map to hold product quantities (productId -> quantity)
            Map<Long, Integer> productQuantities = new HashMap<>();
            
            // Log all parameters for debugging
            logger.info("All form parameters: {}", allParams);
            
            // Extract quantities from the form submission
            for (Long productId : productIds) {
                String quantityKey = "quantity-" + productId;
                if (allParams.containsKey(quantityKey)) {
                    try {
                        int quantity = Integer.parseInt(allParams.get(quantityKey));
                        if (quantity > 0) {
                            productQuantities.put(productId, quantity);
                            logger.info("Found quantity for product {}: {}", productId, quantity);
                        }
                    } catch (NumberFormatException e) {
                        // If parsing fails, default to quantity 1
                        productQuantities.put(productId, 1);
                        logger.warn("Error parsing quantity for product {}, defaulting to 1", productId);
                    }
                } else {
                    // Default quantity if not specified
                    productQuantities.put(productId, 1);
                    logger.warn("No quantity specified for product {}, defaulting to 1", productId);
                }
            }
            
            // Convert product IDs to Product objects with quantities
            List<Product> selectedProducts = productIds.stream()
                .map(id -> {
                    Optional<Product> product = productService.getProductById(id);
                    return product.orElse(null);
                })
                .filter(product -> product != null)
                .collect(Collectors.toList());
            
            try {
                logger.info("Creating order with {} products and the following quantities: {}", 
                           selectedProducts.size(), productQuantities);
                           
                Order order = aiAgentService.createOrderFromAIRecommendationsWithQuantities(
                    user.getId(), selectedProducts, productQuantities, situation);
                redirectAttributes.addFlashAttribute("success", "Order created successfully based on AI recommendations");
                return "redirect:/orders/" + order.getId();
            } catch (Exception e) {
                logger.error("Failed to create order", e);
                redirectAttributes.addFlashAttribute("error", "Failed to create order: " + e.getMessage());
                return "redirect:/ai-shopping";
            }
        }
        
        redirectAttributes.addFlashAttribute("error", "No products selected or user not found");
        return "redirect:/ai-shopping";
    }
    
    @PostMapping("/clear-memory")
    public String clearMemory(Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            aiAgentService.clearUserMemory(user.getId());
            redirectAttributes.addFlashAttribute("success", "AI memory cleared");
        }
        
        return "redirect:/ai-shopping";
    }
}