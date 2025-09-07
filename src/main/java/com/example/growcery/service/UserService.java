package com.example.growcery.service;

import com.example.growcery.dto.UserDTO;
import com.example.growcery.model.Product;
import com.example.growcery.model.User;
import com.example.growcery.repository.ProductRepository;
import com.example.growcery.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void init() {
        // Create admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(User.Role.ADMIN);
            admin.setAddress("Admin Office");
            admin.setMobileNumber("0000000000");
            userRepository.save(admin);
        }
    }

    public User registerUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(User.Role.CUSTOMER);
        user.setAddress(userDTO.getAddress());
        user.setMobileNumber(userDTO.getMobileNumber());
        return userRepository.save(user);
    }

    public List<User> getAllCustomers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == User.Role.CUSTOMER)
                .toList();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
}