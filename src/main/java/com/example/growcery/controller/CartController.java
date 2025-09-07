package com.example.growcery.controller;

import com.example.growcery.model.CartItem;
import com.example.growcery.model.User;
import com.example.growcery.service.CartService;
import com.example.growcery.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<CartItem> cartItems = cartService.getCartItemsByCustomerId(user.getId());
            BigDecimal cartTotal = cartService.calculateCartTotal(user.getId());
            
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", cartTotal);
            return "cart";
        }
        
        return "redirect:/";
    }
    
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                          @RequestParam(defaultValue = "1") Integer quantity,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            cartService.addToCart(user.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Product added to cart");
        }
        
        return "redirect:/products";
    }
    
    @PostMapping("/update")
    public String updateCartItem(@RequestParam Long cartItemId,
                               @RequestParam Integer quantity,
                               RedirectAttributes redirectAttributes) {
        
        boolean updated = cartService.updateCartItemQuantity(cartItemId, quantity);
        
        if (!updated) {
            redirectAttributes.addFlashAttribute("error", "Not enough stock available");
        } else {
            redirectAttributes.addFlashAttribute("success", "Cart updated");
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return "redirect:/cart";
    }
    
    @PostMapping("/clear")
    public String clearCart(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        userOptional.ifPresent(user -> cartService.clearCart(user.getId()));
        
        return "redirect:/cart";
    }
}