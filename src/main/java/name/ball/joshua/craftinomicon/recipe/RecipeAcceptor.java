package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeAcceptor {
    protected static <T> T accept(Recipe recipe, RecipeVisitor<T> visitor) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            return visitor.visit(shapedRecipe);
        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
            return visitor.visit(shapelessRecipe);
        } else if (recipe instanceof FurnaceRecipe) {
            FurnaceRecipe furnaceRecipe = (FurnaceRecipe) recipe;
            return visitor.visit(furnaceRecipe);
        }
        return visitor.visitOther(recipe);
    }
}
