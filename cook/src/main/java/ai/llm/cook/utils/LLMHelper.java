package ai.llm.cook.utils;

import ai.llm.cook.config.OpenAIConfig;
import ai.llm.cook.exception.CustomException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class LLMHelper {

    private final RestTemplate restTemplate;
    private final OpenAIConfig openAIConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public LLMHelper(RestTemplate restTemplate, OpenAIConfig openAIConfig) {
        this.restTemplate = restTemplate;
        this.openAIConfig = openAIConfig;
        this.objectMapper = new ObjectMapper();
    }

    public String generateResponse(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAIConfig.getOpenaiApiKey());

            // Create request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", openAIConfig.getOpenaiModel());
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            ArrayNode messagesArray = objectMapper.createArrayNode();

            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are Cook.ai, a cooking assistant specializing in recipes, cooking techniques, and culinary advice. Provide helpful, accurate, and friendly responses related to cooking.");
            messagesArray.add(systemMessage);

            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesArray.add(userMessage);

            requestBody.set("messages", messagesArray);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    openAIConfig.getOpenaiApiUrl(),
                    request,
                    String.class
            );

            // Parse the response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode choices = responseJson.get("choices");

            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    return message.get("content").asText();
                }
            }

            throw new CustomException("Unable to parse LLM response", null);
        } catch (Exception e) {
            throw new CustomException("Error generating response from LLM: " + e.getMessage(), null);
        }
    }

    public Map<String, Object> generateStructuredResponse(String prompt, String responseFormat) {
        try {
            // Similar to generateResponse but with function calling
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAIConfig.getOpenaiApiKey());

            // Create request body with function calling
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", openAIConfig.getOpenaiModel());
            requestBody.put("temperature", 0.7);

            ArrayNode messagesArray = objectMapper.createArrayNode();

            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are Cook.ai, a cooking assistant. Provide responses in the requested JSON format.");
            messagesArray.add(systemMessage);

            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesArray.add(userMessage);

            requestBody.set("messages", messagesArray);

            // Add response format
            ObjectNode responseFormatNode = objectMapper.createObjectNode();
            responseFormatNode.put("type", "json_object");
            requestBody.set("response_format", responseFormatNode);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    openAIConfig.getOpenaiApiUrl(),
                    request,
                    String.class
            );

            // Parse the response to get the JSON content
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode choices = responseJson.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    String content = message.get("content").asText();
                    return objectMapper.readValue(content, HashMap.class);
                }
            }

            throw new CustomException("Unable to parse LLM structured response", null);
        } catch (Exception e) {
            throw new CustomException("Error generating structured response from LLM: " + e.getMessage(), null);
        }
    }

    public String generateRecipeFromIngredients(String ingredients, String restrictions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Create a recipe using these ingredients: ").append(ingredients).append(". ");

        if (restrictions != null && !restrictions.isEmpty()) {
            prompt.append("Please consider these dietary restrictions: ").append(restrictions).append(". ");
        }

        prompt.append("Format the recipe with a title, ingredients list with quantities, step-by-step instructions, and cooking tips.");

        return generateResponse(prompt.toString());
    }
}
