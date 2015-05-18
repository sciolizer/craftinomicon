package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.DI;
import name.ball.joshua.craftinomicon.di.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;

public class RecipeSnapshotTest extends AbstractTest {

    @Inject private RecipeSnapshot recipeSnapshot;
    @Inject private IngredientsGetter ingredientsGetter;

    @Setup
    public void setup() {
        new DI().injectMembers(this);
    }

    @Test
    public void oakPlanksHaveAtLeastOneRecipe() {
        Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            SortedSet<ItemStack> ingredients = ingredientsGetter.getIngredients(recipe);

            boolean shownOutput = false;
            for (ItemStack ingredient : ingredients) {
                if (ingredient.getType().equals(Material.WOOD)) {
                    if (!shownOutput) {
                        shownOutput = true;
                    }
                    MaterialData data = ingredient.getData();
                }
            }
        }
    }

    @Test
    public void realTest() {
        MaterialRecipes materialRecipes = recipeSnapshot.getMaterialRecipes(new MaterialData(Material.WORKBENCH));
        Collection<Recipe> recipes = materialRecipes.getRecipes();
        assertEqual(recipes.size(), 1);
        Recipe first = recipes.iterator().next();
        SortedSet<ItemStack> ingredients = ingredientsGetter.getIngredients(first);
        for (ItemStack ingredient : ingredients) {
            if (ingredient != null) {
                assertEqual(ingredient.getData().getData(), (byte)-1);
            }
        }

        MaterialRecipes recipes1 = recipeSnapshot.getMaterialRecipes(new MaterialData(Material.WOOD_STAIRS));
        assertEqual(recipes1.getRecipes().size(), 1);
        Recipe theRecipe = recipes1.getRecipes().iterator().next();
        SortedSet<ItemStack> stairsIngredients = ingredientsGetter.getIngredients(theRecipe);
        for (ItemStack stairsIngredient : stairsIngredients) {
            if (stairsIngredient != null) {
                assertEqual(stairsIngredient.getData().getData(), (byte)0);
            }
        }

        Bukkit.getServer().broadcastMessage("realtest passed");
    }

    @Test
    public void testLeatherCapRecipe() {
        for (Material material : new Material[]{Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS}) {
            assertOneRecipe(material);
        }
    }

    @Test
    public void jungleWoodPlanksCanCreateCraftingTableAndJungleWoodStairs() {
        Tree planks = new Tree(Material.WOOD);
        planks.setSpecies(TreeSpecies.JUNGLE);
        MaterialRecipes materialRecipes = recipeSnapshot.getMaterialRecipes(planks);
        boolean foundWorkBench = false;
        boolean foundJungleWoodPlanks = false;
        for (Recipe recipe : materialRecipes.getUsages()) {
            ItemStack result = recipe.getResult();
            if (Material.WORKBENCH.equals(result.getType())) {
                foundWorkBench = true;
            } else if (Material.JUNGLE_WOOD_STAIRS.equals(result.getType())) {
                foundJungleWoodPlanks = true;
            }
        }
        assertEqual(true, foundWorkBench);
        assertEqual(true, foundJungleWoodPlanks);
    }

    protected void assertOneRecipe(Material material) {
        MaterialRecipes materialRecipes = recipeSnapshot.getMaterialRecipes(new MaterialData(material));
        assertEqual(1, materialRecipes.getRecipes().size());
    }


}
