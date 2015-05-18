package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.comparator.MaterialDataComparator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import java.util.*;

public class RecipeSnapshot {

    @Inject private IngredientsGetter ingredientsGetter;
    @Inject private MaterialDataComparator materialDataComparator;
    @Inject private MaterialDataSubstitutes materialDataSubstitutes;
    @Inject private MaterialRecipesFactory materialRecipesFactory;

    private SortedMap<MaterialData, MaterialRecipes> snapshot;

    // todo: actually leather armor does have shapeless recipes (dyed leather), but bukkit is not returning all
    // of the appropriate recipes for them. Also, the current type of RecipeSnapshot.snapshot does not let us
    // disambiguate between different kinds of dyed leather. Also, we'd want to show appropriately colored leather
    // in the output.
    private static final List<Material> NO_SHAPELESS_RECIPES = Arrays.asList(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.FIREWORK);

    private void initialize() {
        if (snapshot != null) return;
        snapshot = new TreeMap<MaterialData, MaterialRecipes>(materialDataComparator);
        Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
        recipes:
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe instanceof ShapelessRecipe && NO_SHAPELESS_RECIPES.contains(recipe.getResult().getType())) {
                continue;
            }
            SortedSet<ItemStack> ingredients = ingredientsGetter.getIngredients(recipe);
            for (ItemStack ingredient : ingredients) {
                if (Material.FIRE.equals(ingredient.getType())) {
                    continue recipes; // skip chain armor recipes
                }
            }
            MaterialData data = recipe.getResult().getData();
            getMaterialRecipes(data).addRecipe(recipe);
            for (ItemStack ingredient : ingredients) {
                for (MaterialRecipes materialRecipes : getAllMaterialRecipes(ingredient.getData())) {
                    materialRecipes.addUsage(recipe);
                }
            }
        }
    }

    // todo: mob drops
    // todo: other ways of getting items

    public MaterialRecipes getMaterialRecipes(MaterialData dataX) {
        initialize();
        MaterialData data = normalize(dataX);
        if (!snapshot.containsKey(data)) {
            snapshot.put(data, materialRecipesFactory.newMaterialRecipes());
        }
        return snapshot.get(data);
    }

    public Set<MaterialData> getAllMaterialsInAtLeastOneRecipe() {
        initialize();
        Set<MaterialData> result = new LinkedHashSet<MaterialData>();
        for (MaterialData materialData : snapshot.keySet()) {
            result.add(normalize(materialData));
        }
        return result;
    }

    private List<MaterialRecipes> getAllMaterialRecipes(MaterialData data) {
        List<MaterialRecipes> results = new ArrayList<MaterialRecipes>();
        for (MaterialData substitute : materialDataSubstitutes.get(data)) {
            results.add(getMaterialRecipes(substitute));
        }
        return results;
    }

    // todo: un-static
    public static MaterialData normalize(MaterialData data) {
        if (data == null) return null;
        if (data.getData() == -1 || data.getItemType().getMaxDurability() > 0) {
            return new MaterialData(data.getItemTypeId(), (byte) 0);
        }
        return data;
    }
}
