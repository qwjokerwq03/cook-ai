package ai.llm.cook.repository;


import ai.llm.cook.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Recipe> findByTitleContainingIgnoreCase(String keyword);
    List<Recipe> findTop10ByOrderByCreatedAtDesc();
}

