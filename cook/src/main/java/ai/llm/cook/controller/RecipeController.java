package ai.llm.cook.controller;

import ai.llm.cook.dto.RecipeRequestDTO;
import ai.llm.cook.dto.RecipeResponseDTO;
import ai.llm.cook.service.RecipeService;
import ai.llm.cook.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final ValidationUtil validationUtil;

    @Autowired
    public RecipeController(RecipeService recipeService, ValidationUtil validationUtil) {
        this.recipeService = recipeService;
        this.validationUtil = validationUtil;
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponseDTO>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> getRecipeById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecipeResponseDTO>> searchRecipes(@RequestParam String keyword) {
        return ResponseEntity.ok(recipeService.searchRecipes(keyword));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecipeResponseDTO>> getRecentRecipes() {
        return ResponseEntity.ok(recipeService.getRecentRecipes());
    }

    @GetMapping("/user")
    public ResponseEntity<List<RecipeResponseDTO>> getUserRecipes(Principal principal) {
        // Assuming principal.getName() returns the user ID as a string
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(recipeService.getUserRecipes(userId));
    }

    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            @RequestBody RecipeRequestDTO recipeRequestDTO,
            Principal principal) {
        validationUtil.validateRecipeRequest(recipeRequestDTO);
        Long userId = Long.parseLong(principal.getName());
        RecipeResponseDTO createdRecipe = recipeService.createRecipe(recipeRequestDTO, userId);
        return new ResponseEntity<>(createdRecipe, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> updateRecipe(
            @PathVariable Long id,
            @RequestBody RecipeRequestDTO recipeRequestDTO,
            Principal principal) {
        validationUtil.validateRecipeRequest(recipeRequestDTO);
        Long userId = Long.parseLong(principal.getName());
        RecipeResponseDTO updatedRecipe = recipeService.updateRecipe(id, recipeRequestDTO, userId);
        return ResponseEntity.ok(updatedRecipe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        recipeService.deleteRecipe(id, userId);
        return ResponseEntity.noContent().build();
    }
}