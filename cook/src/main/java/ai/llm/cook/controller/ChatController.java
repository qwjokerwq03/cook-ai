package ai.llm.cook.controller;

import ai.llm.cook.dto.ChatRequestDTO;
import ai.llm.cook.dto.ChatResponseDTO;
import ai.llm.cook.dto.RecipeResponseDTO;
import ai.llm.cook.service.ChatService;
import ai.llm.cook.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ValidationUtil validationUtil;

    @Autowired
    public ChatController(ChatService chatService, ValidationUtil validationUtil) {
        this.chatService = chatService;
        this.validationUtil = validationUtil;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> processChat(@RequestBody ChatRequestDTO chatRequestDTO) {
        validationUtil.validateChatRequest(chatRequestDTO.getQuery());
        ChatResponseDTO response = chatService.processQuery(chatRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recipe-suggestion")
    public ResponseEntity<String> generateRecipeFromIngredients(
            @RequestParam String ingredients,
            @RequestParam(required = false) String restrictions) {
        String recipeText = chatService.generateRecipeFromIngredients(Arrays.asList(ingredients.split(",")), Arrays.asList(restrictions.split(",")));
        return ResponseEntity.ok(recipeText);
    }
}