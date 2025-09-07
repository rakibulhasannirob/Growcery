package com.example.growcery.controller;

import com.example.growcery.model.Order;
import com.example.growcery.model.OrderItem;
import com.example.growcery.model.User;
import com.example.growcery.service.OrderService;
import com.example.growcery.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String viewOrders(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Order> orders = orderService.getOrdersByCustomerId(user.getId());
            model.addAttribute("orders", orders);
            return "orders";
        }
        
        return "redirect:/";
    }
    
    @GetMapping("/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<Order> orderOptional = orderService.getOrderById(orderId);
            
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                
                // Check if the order belongs to the current user or the user is admin
                if (order.getCustomerId().equals(user.getId()) || user.getRole() == User.Role.ADMIN) {
                    List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(orderId);
                    model.addAttribute("order", order);
                    model.addAttribute("orderItems", orderItems);
                    return "order-details";
                }
            }
        }
        
        return "redirect:/orders";
    }
    
    @PostMapping("/checkout")
    public String checkout(Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            try {
                Order order = orderService.createOrderFromCart(user.getId());
                redirectAttributes.addFlashAttribute("success", "Order placed successfully");
                return "redirect:/orders/" + order.getId();
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/cart";
            }
        }
        
        return "redirect:/cart";
    }
}