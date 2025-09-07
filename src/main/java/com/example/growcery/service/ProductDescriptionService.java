package com.example.growcery.service;

import com.example.growcery.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProductDescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(ProductDescriptionService.class);
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public ProductDescriptionService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    public Map<String, String> generateProductDescription(String productName, String category, String basicDescription) {
        String prompt = composeDescriptionPrompt(productName, category, basicDescription);
        String response = callGeminiAPI(prompt);
        return extractDescriptionAndKeywords(response);
    }
    
    public Map<String, String> generateDescriptionForExistingProduct(Product product) {
        String prompt = composeDescriptionPromptForExisting(product);
        String response = callGeminiAPI(prompt);
        return extractDescriptionAndKeywords(response);
    }
    
    private String composeDescriptionPrompt(String productName, String category, String basicDescription) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a compelling and detailed product description (Within 20 words Stritly) for an e-commerce grocery website for the following product:\n\n")
              .append("Product Name: ").append(productName).append("\n")
              .append("Category: ").append(category).append("\n");
        
        if (basicDescription != null && !basicDescription.isEmpty()) {
            prompt.append("Basic Description: ").append(basicDescription).append("\n\n");
        }
        
        prompt.append("Please provide the following in JSON format:\n")
              .append("1. A rich, engaging product description (Within 20 words Stritly) that highlights nutritional benefits, taste, usage suggestions, and any unique qualities\n")
              .append("2. 5-7 relevant SEO keywords or phrases for this product\n\n")
              .append("Return in this exact JSON format:\n")
              .append("{\n")
              .append("  \"description\": \"Detailed product description here (Within 20 words Stritly)...\",\n")
              .append("  \"keywords\": \"keyword1, keyword2, keyword3, keyword4, keyword5\"\n")
              .append("}");
        
        return prompt.toString();
    }
    
    private String composeDescriptionPromptForExisting(Product product) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Enhance and optimize the following product description (Within 20 words Stritly) for an e-commerce grocery website:\n\n")
              .append("Product Name: ").append(product.getName()).append("\n")
              .append("Category: ").append(product.getCategory()).append("\n");
        
        if (product.getProductDescription() != null && !product.getProductDescription().isEmpty()) {
            prompt.append("Current Description: ").append(product.getProductDescription()).append("\n\n");
        } else {
            prompt.append("Current Description: None\n\n");
        }
        
        prompt.append("Please provide the following in JSON format:\n")
              .append("1. An improved, engaging product description (Within 20 words Stritly) that highlights nutritional benefits, taste, usage suggestions, and any unique qualities. Keep the core information but make it more compelling.\n")
              .append("2. 5-7 relevant SEO keywords or phrases for this product\n\n")
              .append("Return in this exact JSON format:\n")
              .append("{\n")
              .append("  \"description\": \"Detailed product description here (Within 20 words Stritly)...\",\n")
              .append("  \"keywords\": \"keyword1, keyword2, keyword3, keyword4, keyword5\"\n")
              .append("}");
        
        return prompt.toString();
    }
    
    private String callGeminiAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();
            ObjectNode content = objectMapper.createObjectNode();
            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode part = objectMapper.createObjectNode();
            
            part.put("text", prompt);
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            requestBody.set("contents", contents);
            
            String url = GEMINI_URL + "?key=" + apiKey;
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            logger.info("Gemini API response status: {}", response.getStatusCode());
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            
            // Create a simulated response when API fails
            try {
                ObjectNode root = objectMapper.createObjectNode();
                ArrayNode candidates = objectMapper.createArrayNode();
                ObjectNode candidate = objectMapper.createObjectNode();
                ObjectNode candidateContent = objectMapper.createObjectNode();
                ArrayNode parts = objectMapper.createArrayNode();
                ObjectNode part = objectMapper.createObjectNode();
                
                String fallbackJson = "{\n" +
                    "  \"description\": \"This fresh, high-quality product is a perfect addition to your healthy diet. Rich in essential nutrients and vitamins, it provides numerous health benefits. Enjoy it fresh, in salads, or as part of your favorite recipes. Our products are carefully sourced from trusted local farmers to ensure the best quality and taste.\",\n" +
                    "  \"keywords\": \"fresh, organic, healthy, nutritious, vitamin-rich\"\n" +
                    "}";
                
                part.put("text", fallbackJson);
                parts.add(part);
                candidateContent.set("parts", parts);
                candidate.set("content", candidateContent);
                candidates.add(candidate);
                root.set("candidates", candidates);
                
                return objectMapper.writeValueAsString(root);
            } catch (Exception jsonEx) {
                return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"{\\n  \\\"description\\\": \\\"This fresh, high-quality product is perfect for your healthy diet. Rich in essential nutrients and vitamins. Enjoy it fresh, in salads, or as part of your favorite recipes. Our products are carefully sourced from trusted local farmers to ensure the best quality and taste.\\\",\\n  \\\"keywords\\\": \\\"fresh, organic, healthy, nutritious, vitamin-rich\\\"\\n}\"}]}}]}";
            }
        }
    }
    
    private Map<String, String> extractDescriptionAndKeywords(String response) {
        Map<String, String> result = new HashMap<>();
        result.put("description", "This fresh, high-quality product is perfect for your healthy diet.");
        result.put("keywords", "fresh, organic, healthy, nutritious, vitamin-rich");
        
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    
                    // Try to extract JSON from the text
                    try {
                        JsonNode jsonData = objectMapper.readTree(text);
                        if (jsonData.has("description")) {
                            result.put("description", jsonData.get("description").asText());
                        }
                        if (jsonData.has("keywords")) {
                            result.put("keywords", jsonData.get("keywords").asText());
                        }
                    } catch (Exception jsonEx) {
                        logger.warn("Failed to parse description JSON from response: {}", jsonEx.getMessage());
                        
                        // Fallback: Try to extract using string patterns
                        int descStart = text.indexOf("\"description\":");
                        int descEnd = text.indexOf("\"keywords\":");
                        
                        if (descStart >= 0 && descEnd > descStart) {
                            String descPart = text.substring(descStart + 14, descEnd).trim();
                            if (descPart.startsWith("\"") && descPart.endsWith("\",")) {
                                descPart = descPart.substring(1, descPart.length() - 2);
                            }
                            result.put("description", descPart);
                        }
                        
                        int keyStart = text.indexOf("\"keywords\":");
                        if (keyStart >= 0) {
                            String keyPart = text.substring(keyStart + 11).trim();
                            if (keyPart.startsWith("\"") && keyPart.contains("\"")) {
                                keyPart = keyPart.substring(1, keyPart.indexOf("\"", 1));
                            }
                            if (!keyPart.isEmpty()) {
                                result.put("keywords", keyPart);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting description from response: {}", e.getMessage());
        }
        
        return result;
    }
}