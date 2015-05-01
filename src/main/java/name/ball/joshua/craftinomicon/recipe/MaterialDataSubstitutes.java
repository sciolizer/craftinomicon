package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

import java.util.*;

public class MaterialDataSubstitutes {

    protected Map<Material, SortedSet<MaterialData>> substitutes = new HashMap<Material, SortedSet<MaterialData>>();

    public MaterialDataSubstitutes() {
        Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            MaterialData data = recipe.getResult().getData();
            addKnownMaterialData(data);
            for (ItemStack ingredient : RecipeAcceptor.accept(recipe, RecipeSnapshot.ingredientsVisitor)) {
                addKnownMaterialData(ingredient.getData());
            }
        }
    }

    protected void addKnownMaterialData(MaterialData materialData) {
        if (materialData.getData() == (byte)-1) return;
        if (!substitutes.containsKey(materialData.getItemType())) {
            substitutes.put(materialData.getItemType(), new TreeSet<MaterialData>(RecipeSnapshot.materialDataComparator));
        }
        substitutes.get(materialData.getItemType()).add(materialData);
    }

}
