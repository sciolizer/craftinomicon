package name.ball.joshua.craftinomicon.recipe;

import java.util.List;

public interface RecipeScreenFactory {

    RecipeScreen newRecipeScreen(List<MaterialRecipes.IconifiedRecipe> recipes, int offset);

}
