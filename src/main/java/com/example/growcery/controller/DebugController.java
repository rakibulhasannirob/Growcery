package com.example.growcery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Debug controller to log request information.
 * This is a temporary controller to help diagnose 405 errors.
 */
@Controller
@Order(Integer.MAX_VALUE) // Use @Order instead of priority in RequestMapping
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    
    /**
     * Catch-all endpoint for debugging purposes.
     * This will log details about any request that reaches this point.
     */
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST}, 
                   produces = "text/plain")
    @ResponseBody
    public String debugRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n====== DEBUG REQUEST INFO ======\n");
        logMessage.append("Method: ").append(method).append("\n");
        logMessage.append("URI: ").append(uri).append("\n");
        
        if (queryString != null) {
            logMessage.append("Query String: ").append(queryString).append("\n");
        }
        
        logMessage.append("Content Type: ").append(request.getContentType()).append("\n");
        
        // Log headers
        logMessage.append("Headers: \n");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            logMessage.append("  ").append(headerName).append(": ")
                      .append(request.getHeader(headerName)).append("\n");
        });
        
        // Log parameters
        logMessage.append("Parameters: \n");
        request.getParameterMap().forEach((key, values) -> {
            logMessage.append("  ").append(key).append(": ");
            for (String value : values) {
                logMessage.append(value).append(", ");
            }
            logMessage.append("\n");
        });
        
        logMessage.append("==============================\n");
        
        // Log the information
        logger.info(logMessage.toString());
        
        // Return a message that will help diagnose the issue
        return "DEBUG INFO LOGGED: This is a catch-all endpoint to diagnose 405 errors.\n" +
               "The requested endpoint (" + uri + ") does not exist or does not support " + method + " requests.\n" +
               "Please check the server logs for more details.";
    }
}