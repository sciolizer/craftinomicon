package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

import java.util.SortedSet;
import java.util.TreeSet;

public class MaterialRecipes {
    protected MaterialData materialData;

    public MaterialRecipes(MaterialData materialData) {
        this.materialData = materialData;
    }

    public SortedSet<Recipe> recipes = new TreeSet<Recipe>(RecipeSnapshot.recipeComparator);
    public SortedSet<Recipe> usages = new TreeSet<Recipe>(RecipeSnapshot.recipeComparator);

}
