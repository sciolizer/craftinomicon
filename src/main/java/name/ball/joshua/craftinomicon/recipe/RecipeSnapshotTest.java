package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;

import java.util.Iterator;
import java.util.SortedSet;

public class RecipeSnapshotTest extends AbstractTest {

    protected RecipeSnapshot recipeSnapshot;

    @Setup
    public void setup() {
        this.recipeSnapshot = new RecipeSnapshot(new MaterialDataSubstitutes());
        this.recipeSnapshot.initialize();
    }

    @Test
    public void oakPlanksHaveAtLeastOneRecipe() {
        Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            SortedSet<ItemStack> ingredients = RecipeAcceptor.accept(recipe, RecipeSnapshot.ingredientsVisitor);

            boolean shownOutput = false;
            for (ItemStack ingredient : ingredients) {
                if (ingredient.getType().equals(Material.WOOD)) {
                    if (!shownOutput) {
                        shownOutput = true;
                        Bukkit.getServer().broadcastMessage("recipe.getResult() = " + recipe.getResult());
                    }
                    MaterialData data = ingredient.getData();
                    Bukkit.getServer().broadcastMessage("data = " + data + ", class = " + data.getClass());
                }
            }
        }
    }

    @Test
    public void realTest() {

        MaterialRecipes materialRecipes = recipeSnapshot.getMaterialRecipes(new MaterialData(Material.WORKBENCH));
        SortedSet<Recipe> recipes = materialRecipes.recipes;
        assertEqual(recipes.size(), 1);
        Recipe first = recipes.iterator().next();
        SortedSet<ItemStack> ingredients = RecipeAcceptor.accept(first, RecipeSnapshot.ingredientsVisitor);
        for (ItemStack ingredient : ingredients) {
            if (ingredient != null) {
                assertEqual(ingredient.getData().getData(), (byte)-1);
            }
        }

        MaterialRecipes recipes1 = recipeSnapshot.getMaterialRecipes(new MaterialData(Material.WOOD_STAIRS));
        assertEqual(recipes1.recipes.size(), 1);
        Recipe theRecipe = recipes1.recipes.iterator().next();
        SortedSet<ItemStack> stairsIngredients = RecipeAcceptor.accept(theRecipe, RecipeSnapshot.ingredientsVisitor);
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
        for (Recipe recipe : materialRecipes.usages) {
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
        assertEqual(1, materialRecipes.recipes.size());
    }


}
