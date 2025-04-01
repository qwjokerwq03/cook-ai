package ai.llm.cook.service;

import ai.llm.cook.dto.RecipeRequestDTO;
import ai.llm.cook.dto.RecipeResponseDTO;
import ai.llm.cook.exception.CustomException;
import ai.llm.cook.model.Ingredient;
import ai.llm.cook.model.Recipe;
import ai.llm.cook.model.User;
import ai.llm.cook.repository.RecipeRepository;
import ai.llm.cook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    public List<RecipeResponseDTO> getAllRecipes() {
        return recipeRepository.findAll().stream()
                .map(RecipeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public RecipeResponseDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Recipe not found with id: " + id, HttpStatus.NOT_FOUND));
        return RecipeResponseDTO.fromEntity(recipe);
    }

    public List<RecipeResponseDTO> searchRecipes(String keyword) {
        return recipeRepository.findByTitleContainingIgnoreCase(keyword).stream()
                .map(RecipeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public RecipeResponseDTO createRecipe(RecipeRequestDTO recipeDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found with id: " + userId, HttpStatus.NOT_FOUND));

        Recipe recipe = new Recipe();
        recipe.setTitle(recipeDTO.getTitle());
        recipe.setDescription(recipeDTO.getDescription());
        recipe.setInstructions(recipeDTO.getInstructions());
        recipe.setPreparationTime(recipeDTO.getPreparationTime());
        recipe.setCookingTime(recipeDTO.getCookingTime());
        recipe.setServings(recipeDTO.getServings());
        recipe.setUser(user);

        List<Ingredient> ingredients = new ArrayList<>();
        if (recipeDTO.getIngredients() != null) {
            for (RecipeRequestDTO.IngredientDTO ingredientDTO : recipeDTO.getIngredients()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setName(ingredientDTO.getName());
                ingredient.setQuantity(ingredientDTO.getQuantity());
                ingredient.setUnit(ingredientDTO.getUnit());
                ingredient.setRecipe(recipe);
                ingredients.add(ingredient);
            }
        }
        recipe.setIngredients(ingredients);

        Recipe savedRecipe = recipeRepository.save(recipe);
        return RecipeResponseDTO.fromEntity(savedRecipe);
    }

    @Transactional
    public RecipeResponseDTO updateRecipe(Long id, RecipeRequestDTO recipeDTO, Long userId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Recipe not found with id: " + id, HttpStatus.NOT_FOUND));

        if (!recipe.getUser().getId().equals(userId)) {
            throw new CustomException("You are not authorized to update this recipe", HttpStatus.FORBIDDEN);
        }

        recipe.setTitle(recipeDTO.getTitle());
        recipe.setDescription(recipeDTO.getDescription());
        recipe.setInstructions(recipeDTO.getInstructions());
        recipe.setPreparationTime(recipeDTO.getPreparationTime());
        recipe.setCookingTime(recipeDTO.getCookingTime());
        recipe.setServings(recipeDTO.getServings());

        // Clear existing ingredients and add new ones
        recipe.getIngredients().clear();

        if (recipeDTO.getIngredients() != null) {
            for (RecipeRequestDTO.IngredientDTO ingredientDTO : recipeDTO.getIngredients()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setName(ingredientDTO.getName());
                ingredient.setQuantity(ingredientDTO.getQuantity());
                ingredient.setUnit(ingredientDTO.getUnit());
                ingredient.setRecipe(recipe);
                recipe.getIngredients().add(ingredient);
            }
        }

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return RecipeResponseDTO.fromEntity(updatedRecipe);
    }

    @Transactional
    public void deleteRecipe(Long id, Long userId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Recipe not found with id: " + id, HttpStatus.NOT_FOUND));

        if (!recipe.getUser().getId().equals(userId)) {
            throw new CustomException("You are not authorized to delete this recipe", HttpStatus.FORBIDDEN);
        }

        recipeRepository.delete(recipe);
    }

    public List<RecipeResponseDTO> getRecentRecipes() {
        return recipeRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(RecipeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RecipeResponseDTO> getUserRecipes(Long userId) {
        return recipeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(RecipeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

