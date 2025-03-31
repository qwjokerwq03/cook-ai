package ai.llm.cook.utils;

import ai.llm.cook.dto.RecipeRequestDTO;
import ai.llm.cook.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    public void validateRecipeRequest(RecipeRequestDTO recipe) {
        if (recipe == null) {
            throw new CustomException("Recipe cannot be null", HttpStatus.BAD_REQUEST);
        }

        if (recipe.getTitle() == null || recipe.getTitle().trim().isEmpty()) {
            throw new CustomException("Recipe title is required", HttpStatus.BAD_REQUEST);
        }

        if (recipe.getTitle().length() > 100) {
            throw new CustomException("Recipe title cannot exceed 100 characters", HttpStatus.BAD_REQUEST);
        }

        if (recipe.getInstructions() == null || recipe.getInstructions().trim().isEmpty()) {
            throw new CustomException("Recipe instructions are required", HttpStatus.BAD_REQUEST);
        }

        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new CustomException("Recipe must have at least one ingredient", HttpStatus.BAD_REQUEST);
        }

        // Check if preparation time is valid
        if (recipe.getPreparationTime() != null && recipe.getPreparationTime() < 0) {
            throw new CustomException("Preparation time cannot be negative", HttpStatus.BAD_REQUEST);
        }

        // Check if cooking time is valid
        if (recipe.getCookingTime() != null && recipe.getCookingTime() < 0) {
            throw new CustomException("Cooking time cannot be negative", HttpStatus.BAD_REQUEST);
        }

        // Check if servings is valid
        if (recipe.getServings() != null && recipe.getServings() <= 0) {
            throw new CustomException("Servings must be a positive number", HttpStatus.BAD_REQUEST);
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new CustomException("Email cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new CustomException("Invalid email format", HttpStatus.BAD_REQUEST);
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new CustomException("Password cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new CustomException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long", HttpStatus.BAD_REQUEST);
        }

        // Check if password contains at least one digit, one lowercase and one uppercase letter
        if (!password.matches(".*\\d.*") || !password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*")) {
            throw new CustomException("Password must contain at least one digit, one lowercase and one uppercase letter", HttpStatus.BAD_REQUEST);
        }
    }

    public void validateChatRequest(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new CustomException("Query cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (query.length() > 500) {
            throw new CustomException("Query cannot exceed 500 characters", HttpStatus.BAD_REQUEST);
        }
    }
}