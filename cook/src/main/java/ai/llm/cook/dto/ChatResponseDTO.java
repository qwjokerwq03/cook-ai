package ai.llm.cook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ChatResponseDTO {
    private String response;
    private List<RecipeResponseDTO> suggestedRecipes;
    private boolean success;
    private String error;
}

