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
public class ChatRequestDTO {
    private String query;
    private String userId;
    private List<String> dietaryRestrictions;
    private List<String> availableIngredients;
}
