package ai.llm.cook.service;

import ai.llm.cook.config.OpenAIConfig;
import ai.llm.cook.dto.ChatRequestDTO;
import ai.llm.cook.dto.ChatResponseDTO;
import ai.llm.cook.dto.RecipeRequestDTO;
import ai.llm.cook.dto.RecipeResponseDTO;
import ai.llm.cook.model.Ingredient;
import ai.llm.cook.utils.LLMHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final LLMHelper llmHelper;
    private final RecipeService recipeService;
    private final OpenAIConfig openAIConfig;

    @Autowired
    public ChatService(LLMHelper llmHelper, RecipeService recipeService, OpenAIConfig openAIConfig) {
        this.llmHelper = llmHelper;
        this.recipeService = recipeService;
        this.openAIConfig = openAIConfig;
    }

    public ChatResponseDTO processQuery(ChatRequestDTO chatRequest) {
        ChatResponseDTO response = new ChatResponseDTO();

        try {
            // Prepare context for the LLM
            Map<String, Object> context = new HashMap<>();
            context.put("query", chatRequest.getQuery());
            context.put("dietaryRestrictions", chatRequest.getDietaryRestrictions());
            context.put("availableIngredients", chatRequest.getAvailableIngredients());

            // Get response from LLM
            String llmResponse = llmHelper.generateResponse(preparePrompt(context));
            response.setResponse(llmResponse);

            // Try to find relevant recipes based on the query
            List<RecipeResponseDTO> suggestedRecipes = findRelevantRecipes(chatRequest);
            response.setSuggestedRecipes(suggestedRecipes);

            response.setSuccess(true);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError("Failed to process your query: " + e.getMessage());
        }

        return response;
    }

    private String preparePrompt(Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are Cook.ai, a helpful cooking assistant. ");

        String query = (String) context.get("query");
        prompt.append("User query: ").append(query).append("\n\n");

        @SuppressWarnings("unchecked")
        List<String> restrictions = (List<String>) context.get("dietaryRestrictions");
        if (restrictions != null && !restrictions.isEmpty()) {
            prompt.append("Dietary restrictions: ").append(String.join(", ", restrictions)).append("\n\n");
        }

        @SuppressWarnings("unchecked")
        List<String> ingredients = (List<String>) context.get("availableIngredients");
        if (ingredients != null && !ingredients.isEmpty()) {
            prompt.append("Available ingredients: ").append(String.join(", ", ingredients)).append("\n\n");
        }

        prompt.append("Provide a helpful, friendly response about cooking, recipes, or food-related questions. ");
        prompt.append("If the user is asking for a recipe, provide detailed instructions, ingredients list, and cooking tips.");

        return prompt.toString();
    }

    private List<RecipeResponseDTO> findRelevantRecipes(ChatRequestDTO chatRequest) {
        // Extract keywords from query to find relevant recipes
        String query = chatRequest.getQuery().toLowerCase();
        List<RecipeResponseDTO> results = new ArrayList<>();

        // Simple keyword extraction - in a real application, use NLP or a more sophisticated approach
        String[] keywords = extractKeywords(query);

        for (String keyword : keywords) {
            if (keyword.length() > 3) { // Ignore short words
                List<RecipeResponseDTO> found = recipeService.searchRecipes(keyword);
                if (!found.isEmpty()) {
                    results.addAll(found);
                    if (results.size() >= 5) { // Limit to 5 suggestions
                        break;
                    }
                }
            }
        }

        return results.stream().distinct().limit(5).toList();
    }

    private String[] extractKeywords(String query) {
        // Remove common words and split by spaces
        // In a real app, use a proper NLP library for this
        String cleaned = query.replaceAll("how|what|when|where|why|can|you|the|for|and|with|recipe|make|cook", "");
        return cleaned.split("\\s+");
    }

    public String generateRecipeFromIngredients(List<String> ingredients, List<String> restrictions) {
        RecipeResponseDTO generatedRecipe = new RecipeResponseDTO();

        try {
            // Prepare context for the LLM
            Map<String, Object> context = new HashMap<>();
            context.put("availableIngredients", ingredients);
            context.put("dietaryRestrictions", restrictions);

            // Create a specialized prompt for recipe generation
            String prompt = prepareRecipeGenerationPrompt(ingredients, restrictions);

            // Get response from LLM
            String llmResponse = llmHelper.generateResponse(prompt);

            // Parse the LLM response to create a structured recipe
            generatedRecipe = parseRecipeFromLLMResponse(llmResponse);
            generatedRecipe.setSuccess(true);
        } catch (Exception e) {
            generatedRecipe.setSuccess(false);
            generatedRecipe.setError("Failed to generate recipe: " + e.getMessage());
        }

        return generatedRecipe.toString();
    }

    private String prepareRecipeGenerationPrompt(List<String> ingredients, List<String> restrictions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are Cook.ai, a creative chef assistant. ");
        prompt.append("Generate a complete recipe using only these ingredients: ");
        prompt.append(String.join(", ", ingredients)).append("\n\n");

        if (restrictions != null && !restrictions.isEmpty()) {
            prompt.append("The recipe must respect these dietary restrictions: ");
            prompt.append(String.join(", ", restrictions)).append("\n\n");
        }

        prompt.append("Your response should follow this structure:\n");
        prompt.append("TITLE: [Recipe Name]\n");
        prompt.append("DESCRIPTION: [Brief description]\n");
        prompt.append("INGREDIENTS: [List all ingredients with measurements]\n");
        prompt.append("INSTRUCTIONS: [Step by step cooking instructions]\n");
        prompt.append("COOKING_TIME: [Total time in minutes]\n");
        prompt.append("DIFFICULTY: [Easy, Medium, or Hard]\n");
        prompt.append("CUISINE: [Type of cuisine]\n");

        prompt.append("\nBe creative but practical. Ensure the recipe is delicious and feasible with only the provided ingredients ");
        if (restrictions != null && !restrictions.isEmpty()) {
            prompt.append("while strictly adhering to all dietary restrictions.");
        } else {
            prompt.append(".");
        }

        return prompt.toString();
    }

    private String prepareRecipeGenerationPrompt(List<String> ingredients) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are Cook.ai, a creative chef assistant. ");
        prompt.append("Generate a complete recipe using only these ingredients: ");
        prompt.append(String.join(", ", ingredients)).append("\n\n");

        prompt.append("Your response should follow this structure:\n");
        prompt.append("TITLE: [Recipe Name]\n");
        prompt.append("DESCRIPTION: [Brief description]\n");
        prompt.append("INGREDIENTS: [List all ingredients with measurements]\n");
        prompt.append("INSTRUCTIONS: [Step by step cooking instructions]\n");
        prompt.append("COOKING_TIME: [Total time in minutes]\n");
        prompt.append("DIFFICULTY: [Easy, Medium, or Hard]\n");
        prompt.append("CUISINE: [Type of cuisine]\n");

        prompt.append("\nBe creative but practical. Ensure the recipe is delicious and feasible with only the provided ingredients.");

        return prompt.toString();
    }

    private RecipeResponseDTO parseRecipeFromLLMResponse(String llmResponse) {
        RecipeResponseDTO recipe = new RecipeResponseDTO();

        // Parse the structured response from LLM
        // This is a simplified implementation
        // In a real app, use regex or a more robust parsing approach

        // Extract title
        if (llmResponse.contains("TITLE:")) {
            String[] parts = llmResponse.split("TITLE:");
            if (parts.length > 1) {
                String titlePart = parts[1].split("\n")[0].trim();
                recipe.setTitle(titlePart);
            }
        }

        // Extract description
        if (llmResponse.contains("DESCRIPTION:")) {
            String[] parts = llmResponse.split("DESCRIPTION:");
            if (parts.length > 1) {
                String[] lines = parts[1].split("INGREDIENTS:");
                if (lines.length > 0) {
                    recipe.setDescription(lines[0].trim());
                }
            }
        }

        // Extract ingredients as a list
        if (llmResponse.contains("INGREDIENTS:")) {
            String[] parts = llmResponse.split("INGREDIENTS:");
            if (parts.length > 1) {
                String[] ingredientSection = parts[1].split("INSTRUCTIONS:");
                if (ingredientSection.length > 0) {
                    String[] ingredientLines = ingredientSection[0].trim().split("\n");
                    List<RecipeRequestDTO.IngredientDTO> ingredientsList = new ArrayList<>();
                    for (String line : ingredientLines) {
                        if (!line.trim().isEmpty()) {
                            ingredientsList.add(RecipeRequestDTO.IngredientDTO.builder().description(line.trim()).build());
                        }
                    }
                    recipe.setIngredients(ingredientsList);
                }
            }
        }

        // Extract instructions as a list
        if (llmResponse.contains("INSTRUCTIONS:")) {
            String[] parts = llmResponse.split("INSTRUCTIONS:");
            if (parts.length > 1) {
                String[] instructionSection = parts[1].split("COOKING_TIME:");
                if (instructionSection.length > 0) {
                    String[] instructionLines = instructionSection[0].trim().split("\n");
                    List<String> instructionsList = new ArrayList<>();
                    for (String line : instructionLines) {
                        if (!line.trim().isEmpty()) {
                            instructionsList.add(line.trim());
                        }
                    }
                    recipe.setInstructions(instructionsList.toString());
                }
            }
        }

        // Extract cooking time
        if (llmResponse.contains("COOKING_TIME:")) {
            String[] parts = llmResponse.split("COOKING_TIME:");
            if (parts.length > 1) {
                String timePart = parts[1].split("\n")[0].trim();
                try {
                    int cookingTime = Integer.parseInt(timePart.replaceAll("\\D+", ""));
                    recipe.setCookingTime(cookingTime);
                } catch (NumberFormatException e) {
                    recipe.setCookingTime(0);
                }
            }
        }

        // Extract difficulty
        if (llmResponse.contains("DIFFICULTY:")) {
            String[] parts = llmResponse.split("DIFFICULTY:");
            if (parts.length > 1) {
                String difficultyPart = parts[1].split("\n")[0].trim();
                recipe.setExtra("DifficultyPart : "+difficultyPart);
            }
        }

        // Extract cuisine type
        if (llmResponse.contains("CUISINE:")) {
            String[] parts = llmResponse.split("CUISINE:");
            if (parts.length > 1) {
                String cuisinePart = parts[1].split("\n")[0].trim();
                recipe.setExtra("CUISINE : "+cuisinePart);
            }
        }

        return recipe;
    }
}