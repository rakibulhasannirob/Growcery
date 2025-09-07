package com.example.growcery.controller;

import com.example.growcery.dto.ProductDTO;
import com.example.growcery.model.Order;
import com.example.growcery.model.Product;
import com.example.growcery.model.User;
import com.example.growcery.service.OrderService;
import com.example.growcery.service.ProductDescriptionService;
import com.example.growcery.service.ProductService;
import com.example.growcery.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ProductDescriptionService productDescriptionService;
    private final HttpServletRequest request;

    public AdminController(UserService userService, 
                         ProductService productService, 
                         OrderService orderService,
                         ProductDescriptionService productDescriptionService,
                         HttpServletRequest request) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.productDescriptionService = productDescriptionService;
        this.request = request;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Product> products = productService.getAllProducts();
        List<User> customers = userService.getAllCustomers();
        List<Order> orders = orderService.getAllOrders();
        
        model.addAttribute("productsCount", products.size());
        model.addAttribute("customersCount", customers.size());
        model.addAttribute("ordersCount", orders.size());
        
        return "admin/dashboard";
    }
    
    // Product Management
    @GetMapping("/products")
    public String productList(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/products";
    }
    
    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("categories", Product.Category.values());
        return "admin/product-form";
    }
    
    @PostMapping("/products/add")
    public String addProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                          BindingResult result,
                          @RequestParam(value = "generateDescription", required = false) boolean generateDescription,
                          Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("categories", Product.Category.values());
            return "admin/product-form";
        }
        
        // Generate product description if requested
        if (generateDescription && (productDTO.getProductDescription() == null 
                || productDTO.getProductDescription().trim().isEmpty())) {
            // Call the description service
            try {
                Map<String, String> generatedContent = productDescriptionService.generateProductDescription(
                        productDTO.getName(), 
                        productDTO.getCategory().toString(), 
                        null);
                
                productDTO.setProductDescription(generatedContent.get("description"));
                productDTO.setSeoKeywords(generatedContent.get("keywords"));
            } catch (Exception e) {
                // Log error but continue with product creation
                logger.error("Failed to generate product description: {}", e.getMessage());
            }
        }
        
        // If this is coming from the AI generator with a description field
        if (productDTO.getProductDescription() == null && request.getParameter("description") != null) {
            productDTO.setProductDescription(request.getParameter("description"));
        }
        
        // If this is coming from the AI generator with keywords
        if (productDTO.getSeoKeywords() == null && request.getParameter("keywords") != null) {
            productDTO.setSeoKeywords(request.getParameter("keywords"));
        }
        
        productService.saveProduct(productDTO);
        
        
        if (generateDescription) {
            request.getSession().setAttribute("success", "Product saved with AI-generated description and SEO keywords!");
        } else {
            request.getSession().setAttribute("success", "Product saved successfully!");
        }
    
        return "redirect:/admin/products";
       
    }
    
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Optional<Product> productOptional = productService.getProductById(id);
        
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            
            // Create and populate ProductDTO
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setProductDescription(product.getProductDescription());
            productDTO.setCategory(product.getCategory());
            productDTO.setPrice(product.getPrice());
            productDTO.setStock(product.getStock());
            if (product.getSeoKeywords() != null) {
                productDTO.setSeoKeywords(product.getSeoKeywords());
            }
            
            model.addAttribute("productDTO", productDTO);
            model.addAttribute("categories", Product.Category.values());
            return "admin/product-form";
        }
        
        return "redirect:/admin/products";
    }
    
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }
    
    // Customer Management
    @GetMapping("/customers")
    public String customerList(Model model) {
        List<User> customers = userService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "admin/customers";
    }
    
    @GetMapping("/customers/{id}/orders")
    public String customerOrders(@PathVariable Long id, Model model) {
        Optional<User> customerOptional = userService.getUserById(id);
        
        if (customerOptional.isPresent()) {
            User customer = customerOptional.get();
            List<Order> orders = orderService.getOrdersByCustomerId(customer.getId());
            
            model.addAttribute("customer", customer);
            model.addAttribute("orders", orders);
            return "admin/customer-orders";
        }
        
        return "redirect:/admin/customers";
    }
    
    // Order Management
    @GetMapping("/orders")
    public String orderList(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }
    
    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        Optional<Order> orderOptional = orderService.getOrderById(id);
        
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            Optional<User> customerOptional = userService.getUserById(order.getCustomerId());
            
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderService.getOrderItemsByOrderId(id));
            customerOptional.ifPresent(customer -> model.addAttribute("customer", customer));
            
            return "admin/order-details";
        }
        
        return "redirect:/admin/orders";
    }
}