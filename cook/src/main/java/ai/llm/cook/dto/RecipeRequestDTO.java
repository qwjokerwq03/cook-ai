package ai.llm.cook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeRequestDTO {
    private String title;
    private String description;
    private String instructions;
    private Integer preparationTime;
    private Integer cookingTime;
    private Integer servings;
    private List<IngredientDTO> ingredients;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IngredientDTO {
        private String name;
        private String quantity;
        private String unit;
        private String description;

    }
}
