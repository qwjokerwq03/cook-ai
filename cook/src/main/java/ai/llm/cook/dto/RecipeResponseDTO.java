package ai.llm.cook.dto;

import ai.llm.cook.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String instructions;
    private Integer preparationTime;
    private Integer cookingTime;
    private Integer servings;
    private List<RecipeRequestDTO.IngredientDTO> ingredients;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String extra;
    private boolean success;
    private String error;

    public static RecipeResponseDTO fromEntity(Recipe recipe) {
        RecipeResponseDTO dto = new RecipeResponseDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setInstructions(recipe.getInstructions());
        dto.setPreparationTime(recipe.getPreparationTime());
        dto.setCookingTime(recipe.getCookingTime());
        dto.setServings(recipe.getServings());
        dto.setAuthorName(recipe.getUser() != null ? recipe.getUser().getFullName() : "Anonymous");
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setUpdatedAt(recipe.getUpdatedAt());

        if (recipe.getIngredients() != null) {
            dto.setIngredients(recipe.getIngredients().stream()
                    .map(ingredient -> {
                        RecipeRequestDTO.IngredientDTO ingredientDTO = new RecipeRequestDTO.IngredientDTO();
                        ingredientDTO.setName(ingredient.getName());
                        ingredientDTO.setQuantity(ingredient.getQuantity());
                        ingredientDTO.setUnit(ingredient.getUnit());
                        return ingredientDTO;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }

}