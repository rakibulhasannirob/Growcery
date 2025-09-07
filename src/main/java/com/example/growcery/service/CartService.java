package com.example.growcery.service;

import com.example.growcery.model.CartItem;
import com.example.growcery.model.Product;
import com.example.growcery.repository.CartItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    public List<CartItem> getCartItemsByCustomerId(Long customerId) {
        List<CartItem> cartItems = cartItemRepository.findByCustomerId(customerId);
        
        // Fetch product details for each cart item
        for (CartItem cartItem : cartItems) {
            productService.getProductById(cartItem.getProductId())
                    .ifPresent(cartItem::setProduct);
        }
        
        return cartItems;
    }

    @Transactional
    public void addToCart(Long customerId, Long productId, Integer quantity) {
        Optional<CartItem> existingItem = cartItemRepository.findByCustomerIdAndProductId(customerId, productId);
        
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCustomerId(customerId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    public boolean updateCartItemQuantity(Long cartItemId, Integer quantity) {
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartItemId);
        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            
            // Check if the product has enough stock
            if (productService.checkStock(cartItem.getProductId(), quantity)) {
                cartItem.setQuantity(quantity);
                cartItemRepository.save(cartItem);
                return true;
            }
        }
        return false;
    }

    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
    }

    public BigDecimal calculateCartTotal(Long customerId) {
        List<CartItem> cartItems = getCartItemsByCustomerId(customerId);
        return cartItems.stream()
                .map(item -> {
                    Product product = item.getProduct();
                    if (product != null) {
                        return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean validateCartStock(Long customerId) {
        List<CartItem> cartItems = cartItemRepository.findByCustomerId(customerId);
        
        for (CartItem item : cartItems) {
            if (!productService.checkStock(item.getProductId(), item.getQuantity())) {
                return false;
            }
        }
        
        return true;
    }
}