package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.comparator.RecipeComparator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

import java.util.*;

import static name.ball.joshua.craftinomicon.recipe.RecipeSnapshot.normalize;

public class MaterialRecipes {

    @Inject private IngredientsGetter ingredientsGetter;
    @Inject private RecipeComparator recipeComparator;

    private final SortedSet<IconifiedRecipe> recipes;
    private final SortedSet<IconifiedRecipe> usages;

    public MaterialRecipes() {
        this.recipes = new TreeSet<IconifiedRecipe>();
        this.usages = new TreeSet<IconifiedRecipe>();
    }

    public Collection<Recipe> getRecipes() {
        return getRecipes(recipes);
    }

    public Collection<Recipe> getUsages() {
        return getRecipes(usages);
    }

    public Set<IconifiedRecipe> getRecipesWithIcons() {
        return Collections.unmodifiableSet(recipes);
    }

    public Set<IconifiedRecipe> getUsagesWithIcons() {
        return Collections.unmodifiableSet(usages);
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(new IconifiedRecipe(null, recipe));
        Map<MaterialData,Integer> counts = new HashMap<MaterialData, Integer>();
        for (IconifiedRecipe iconifiedRecipe : recipes) {
            SortedSet<ItemStack> ingredients = ingredientsGetter.getIngredients(iconifiedRecipe.recipe);
            for (ItemStack ingredient : ingredients) {
                MaterialData data = ingredient.getData();
                if (!counts.containsKey(data)) {
                    counts.put(data, 0);
                }
                counts.put(data, counts.get(data) + 1);
            }
        }
        for (IconifiedRecipe iconifiedRecipe : recipes) {
            SortedSet<ItemStack> ingredients = ingredientsGetter.getIngredients(iconifiedRecipe.recipe);
            int lowestCount = Integer.MAX_VALUE;
            ItemStack stack = null;
            for (ItemStack ingredient : ingredients) {
                Integer curCount = counts.get(ingredient.getData());
                if (curCount < lowestCount) {
                    lowestCount = curCount;
                    stack = ingredient;
                }
            }
            iconifiedRecipe.icon = normalize(stack.getData()).toItemStack(1);
        }
    }

    public void addUsage(Recipe recipe) {
        usages.add(new IconifiedRecipe(recipe.getResult(), recipe));
    }

    private Collection<Recipe> getRecipes(SortedSet<IconifiedRecipe> entries) {
        List<Recipe> result = new ArrayList<Recipe>(entries.size());
        for (IconifiedRecipe entry : entries) {
            result.add(entry.recipe);
        }
        return Collections.unmodifiableList(result);
    }

    public class IconifiedRecipe implements Comparable<IconifiedRecipe> {
        private ItemStack icon;
        private final Recipe recipe;

        public IconifiedRecipe(ItemStack icon, Recipe recipe) {
            this.icon = icon;
            this.recipe = recipe;
        }

        @Override
        public int compareTo(IconifiedRecipe iconifiedRecipe) {
            return recipeComparator.compare(this.recipe, iconifiedRecipe.recipe);
        }

        public ItemStack getIcon() {
            return icon;
        }

        public Recipe getRecipe() {
            return recipe;
        }
    }
}
