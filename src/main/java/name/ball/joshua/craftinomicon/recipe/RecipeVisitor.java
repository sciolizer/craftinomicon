package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public interface RecipeVisitor<T> {
    T visit(ShapedRecipe shapedRecipe);
    T visit(ShapelessRecipe shapelessRecipe);
    T visit(FurnaceRecipe furnaceRecipe);
    T visitOther(Recipe recipe);
}
