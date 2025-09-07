package com.example.growcery.controller;

import com.example.growcery.dto.UserDTO;
import com.example.growcery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                              BindingResult result, Model model) {
        
        if (result.hasErrors()) {
            return "register";
        }
        
        if (userService.existsByUsername(userDTO.getUsername())) {
            model.addAttribute("usernameError", "Username is already taken");
            return "register";
        }
        
        userService.registerUser(userDTO);
        return "redirect:/login?registered";
    }
}