package com.example.growcery.controller;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(
                webRequest, org.springframework.boot.web.error.ErrorAttributeOptions.of(
                        org.springframework.boot.web.error.ErrorAttributeOptions.Include.STACK_TRACE,
                        org.springframework.boot.web.error.ErrorAttributeOptions.Include.MESSAGE,
                        org.springframework.boot.web.error.ErrorAttributeOptions.Include.EXCEPTION));
        
        // Get error status
        Integer status = (Integer) errorAttributes.get("status");
        String error = (String) errorAttributes.get("error");
        String message = (String) errorAttributes.get("message");
        String trace = (String) errorAttributes.get("trace");
        String path = (String) errorAttributes.get("path");
        
        // Only include stack trace in development
        boolean includeStackTrace = true; // Set to false in production
        
        model.addAttribute("status", status);
        model.addAttribute("error", error);
        model.addAttribute("message", message);
        model.addAttribute("path", path);
        
        if (includeStackTrace) {
            model.addAttribute("trace", trace);
        }
        
        // Add a more user-friendly message based on status code
        if (status != null) {
            switch (HttpStatus.valueOf(status)) {
                case NOT_FOUND:
                    model.addAttribute("friendlyMessage", "The page you were looking for could not be found.");
                    break;
                case FORBIDDEN:
                    model.addAttribute("friendlyMessage", "You don't have permission to access this resource.");
                    break;
                case INTERNAL_SERVER_ERROR:
                    model.addAttribute("friendlyMessage", "Something went wrong on our end. We're working on it!");
                    break;
                default:
                    model.addAttribute("friendlyMessage", "An error occurred. Please try again later.");
                    break;
            }
        }
        
        return "error";
    }
}