package com.example.growcery.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Filter to log HTTP request details for debugging purposes.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Only log POST requests to help diagnose 405 errors
        if ("POST".equals(request.getMethod())) {
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            
            // Log before processing
            logRequest(wrappedRequest);
            
            try {
                // Continue with the request
                filterChain.doFilter(wrappedRequest, response);
            } finally {
                // Log the status code after processing
                logger.info("Response Status: {}", response.getStatus());
            }
        } else {
            // For non-POST requests, just continue without logging
            filterChain.doFilter(request, response);
        }
    }
    
    private void logRequest(HttpServletRequest request) {
        StringBuilder message = new StringBuilder();
        message.append("\n===================================");
        message.append("\nREQUEST DETAILS:");
        message.append("\n===================================");
        message.append("\nMethod: ").append(request.getMethod());
        message.append("\nURI: ").append(request.getRequestURI());
        message.append("\nQuery: ").append(request.getQueryString() != null ? request.getQueryString() : "");
        message.append("\nHeaders: ");
        
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            message.append("\n  ").append(headerName).append(": ").append(request.getHeader(headerName));
        }
        
        message.append("\nParameters: ");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            message.append("\n  ").append(paramName).append(": ");
            
            String[] paramValues = request.getParameterValues(paramName);
            for (String value : paramValues) {
                message.append(value).append(", ");
            }
        }
        
        message.append("\n===================================");
        
        logger.info(message.toString());
    }
}