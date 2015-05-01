package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.inventory.Recipe;

import java.util.List;

public interface RecipeScreenFactory {

    RecipeScreen newRecipeScreen(List<Recipe> recipes, int offset);

}
