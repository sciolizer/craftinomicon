package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.comparator.MaterialDataComparator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

import java.util.*;

public class MaterialDataSubstitutes {

    @Inject private IngredientsGetter ingredientsGetter;
    @Inject private MaterialDataComparator materialDataComparator;

    private Map<Material, SortedSet<MaterialData>> substitutes;

    private void initialize() {
        if (substitutes != null) return;
        substitutes = new LinkedHashMap<Material, SortedSet<MaterialData>>();
        Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            MaterialData data = recipe.getResult().getData();
            addKnownMaterialData(data);
            for (ItemStack ingredient : ingredientsGetter.getIngredients(recipe)) {
                addKnownMaterialData(ingredient.getData());
            }
        }
    }

    public List<MaterialData> get(MaterialData data) {
        initialize();
        Material material = data.getItemType();
        if (data.getData() != (byte) -1 || !substitutes.containsKey(material)) {
            return Collections.singletonList(data);
        }
        return new ArrayList<MaterialData>(substitutes.get(material));
    }

    private void addKnownMaterialData(MaterialData materialData) {
        if (materialData.getData() == (byte)-1) return;
        if (!substitutes.containsKey(materialData.getItemType())) {
            substitutes.put(materialData.getItemType(), new TreeSet<MaterialData>(materialDataComparator));
        }
        substitutes.get(materialData.getItemType()).add(materialData);
    }

}
