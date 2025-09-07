package com.example.growcery.service;

import com.example.growcery.model.CartItem;
import com.example.growcery.model.Order;
import com.example.growcery.model.OrderItem;
import com.example.growcery.model.Product;
import com.example.growcery.repository.OrderItemRepository;
import com.example.growcery.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, 
                       OrderItemRepository orderItemRepository,
                       CartService cartService,
                       ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.productService = productService;
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        
        // Fetch product details for each order item
        for (OrderItem orderItem : orderItems) {
            productService.getProductById(orderItem.getProductId())
                    .ifPresent(orderItem::setProduct);
        }
        
        return orderItems;
    }

    @Transactional
    public Order createOrderFromCart(Long customerId) {
        // Validate cart stock first
        if (!cartService.validateCartStock(customerId)) {
            throw new RuntimeException("Some products in your cart are out of stock");
        }

        List<CartItem> cartItems = cartService.getCartItemsByCustomerId(customerId);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Create a new order
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(cartService.calculateCartTotal(customerId));
        order.setStatus(Order.OrderStatus.SUCCESSFUL);
        
        Order savedOrder = orderRepository.save(order);

        // Create order items from cart items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(savedOrder.getId());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            
            // Get product price
            Optional<Product> optionalProduct = productService.getProductById(cartItem.getProductId());
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                orderItem.setPrice(product.getPrice());
                
                // Update product stock
                productService.updateStock(product.getId(), cartItem.getQuantity());
            }
            
            orderItemRepository.save(orderItem);
        }

        // Clear the cart
        cartService.clearCart(customerId);

        return savedOrder;
    }
}