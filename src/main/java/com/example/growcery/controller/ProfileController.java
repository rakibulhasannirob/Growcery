package com.example.growcery.controller;

import com.example.growcery.model.User;
import com.example.growcery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "profile";
        }
        
        return "redirect:/";
    }
    
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "profile";
        }
        
        String username = authentication.getName();
        Optional<User> currentUserOptional = userService.getUserByUsername(username);
        
        if (currentUserOptional.isPresent()) {
            User currentUser = currentUserOptional.get();
            
            // Only update these fields
            currentUser.setAddress(user.getAddress());
            currentUser.setMobileNumber(user.getMobileNumber());
            
            userService.updateUser(currentUser);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        }
        
        return "redirect:/profile";
    }
}