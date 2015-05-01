package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.material.MaterialData;

import java.util.*;

public class RecipeSnapshot {

    protected MaterialDataSubstitutes materialDataSubstitutes;
    protected SortedMap<MaterialData, MaterialRecipes> snapshot = new TreeMap<MaterialData, MaterialRecipes>(materialDataComparator);

    public RecipeSnapshot(MaterialDataSubstitutes materialDataSubstitutes) {
        this.materialDataSubstitutes = materialDataSubstitutes;
    }

    // todo: actually leather armor does have shapeless recipes (dyed leather), but bukkit is not returning all
    // of the appropriate recipes for them. Also, the current type of RecipeSnapshot.snapshot does not let us
    // disambiguate between different kinds of dyed leather. Also, we'd want to show appropriately colored leather
    // in the output.
    private static final List<Material> NO_SHAPELESS_RECIPES = Arrays.asList(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.FIREWORK);

    public void initialize() {
        Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
        recipes:
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe instanceof ShapelessRecipe && NO_SHAPELESS_RECIPES.contains(recipe.getResult().getType())) {
                continue;
            }
            SortedSet<ItemStack> ingredients = RecipeAcceptor.accept(recipe, ingredientsVisitor);
            for (ItemStack ingredient : ingredients) {
                if (Material.FIRE.equals(ingredient.getType())) {
                    continue recipes; // skip chain armor recipes
                }
            }
            MaterialData data = recipe.getResult().getData();
            getMaterialRecipes(data).recipes.add(recipe);
            for (ItemStack ingredient : ingredients) {
                for (MaterialRecipes materialRecipes : getAllMaterialRecipes(ingredient.getData())) {
                    materialRecipes.usages.add(recipe);
                }
            }
        }
    }

    // todo: mob drops
    // todo: other ways of getting items

    protected MaterialRecipes getMaterialRecipes(MaterialData dataX) {
        MaterialData data = normalize(dataX);
        if (!snapshot.containsKey(data)) {
            snapshot.put(data, new MaterialRecipes(data));
        }
        return snapshot.get(data);
    }

    protected List<MaterialRecipes> getAllMaterialRecipes(MaterialData data) {
        if (data.getData() != (byte) -1) {
            return Collections.singletonList(getMaterialRecipes(data));
        }
        SortedSet<MaterialData> substitutes = materialDataSubstitutes.substitutes.get(data.getItemType());
        if (substitutes == null || substitutes.isEmpty()) {
            return Collections.singletonList(getMaterialRecipes(data));
        }
        List<MaterialRecipes> result = new ArrayList<MaterialRecipes>(substitutes.size());
        for (MaterialData substitute : substitutes) {
            result.add(getMaterialRecipes(substitute));
        }
        return result;
    }

    protected static final Comparator<MaterialData> materialDataComparator = new Comparator<MaterialData>() {
        @Override
        public int compare(MaterialData o2, MaterialData o1) {
            if (o1 == o2) return 0;
            int itemTypeDiff = o2.getItemTypeId() - o1.getItemTypeId();
            if (itemTypeDiff != 0) {
                return itemTypeDiff;
            }
            return (int) o2.getData() - (int) o1.getData();
        }
    };

    protected static final Comparator<Recipe> recipeComparator = new Comparator<Recipe>() {
        @Override
        public int compare(Recipe o1, Recipe o2) {
            if (o1 == o2) return 0;
            int recipeTypeDiff = RecipeAcceptor.accept(o2, recipeTypeIndexVisitor) - RecipeAcceptor.accept(o1, recipeTypeIndexVisitor);
            if (recipeTypeDiff != 0) {
                return -1 * recipeTypeDiff;
            }
            int outputDiff = materialDataComparator.compare(o1.getResult().getData(), o2.getResult().getData());
            if (outputDiff != 0) {
                return outputDiff;
            }
            SortedSet<ItemStack> ingredients1 = RecipeAcceptor.accept(o1, ingredientsVisitor);
            SortedSet<ItemStack> ingredients2 = RecipeAcceptor.accept(o2, ingredientsVisitor);
            return iterableComparator(itemStackComparator).compare(ingredients1, ingredients2);
        }
    };

    protected static RecipeVisitor<Integer> recipeTypeIndexVisitor = new RecipeVisitor<Integer>() {
        @Override
        public Integer visit(ShapedRecipe shapedRecipe) {
            return 0;
        }

        @Override
        public Integer visit(ShapelessRecipe shapelessRecipe) {
            return 1;
        }

        @Override
        public Integer visit(FurnaceRecipe furnaceRecipe) {
            return 2;
        }

        @Override
        public Integer visitOther(Recipe recipe) {
            return 3 + Math.abs(recipe.getClass().hashCode());
        }
    };

    protected static RecipeVisitor<SortedSet<ItemStack>> ingredientsVisitor = new RecipeVisitor<SortedSet<ItemStack>>() {
        @Override
        public SortedSet<ItemStack> visit(ShapedRecipe shapedRecipe) {
            TreeSet<ItemStack> treeSet = new TreeSet<ItemStack>(itemStackComparator);
            for (ItemStack value : shapedRecipe.getIngredientMap().values()) {
                if (value != null) {
                    treeSet.add(value);
                }
            }
            return treeSet;
        }

        @Override
        public SortedSet<ItemStack> visit(ShapelessRecipe shapelessRecipe) {
            TreeSet<ItemStack> treeSet = new TreeSet<ItemStack>(itemStackComparator);
            treeSet.addAll(shapelessRecipe.getIngredientList());
            return treeSet;
        }

        @Override
        public SortedSet<ItemStack> visit(FurnaceRecipe furnaceRecipe) {
            TreeSet<ItemStack> treeSet = new TreeSet<ItemStack>(itemStackComparator);
            treeSet.add(furnaceRecipe.getInput());
            return treeSet;
        }

        @Override
        public SortedSet<ItemStack> visitOther(Recipe recipe) {
            return new TreeSet<ItemStack>(itemStackComparator);
        }
    };

    protected static <T> Comparator<Iterable<T>> iterableComparator(final Comparator<T> innerComparator) {
        return new Comparator<Iterable<T>>() {
            @Override
            public int compare(Iterable<T> o1, Iterable<T> o2) {
                if (o1 == o2) return 0;
                Iterator<T> iterator1 = o1.iterator();
                Iterator<T> iterator2 = o2.iterator();
                while (iterator1.hasNext()) {
                    T next1 = iterator1.next();
                    if (!iterator2.hasNext()) {
                        return 1;
                    }
                    T next2 = iterator2.next();
                    int compare = innerComparator.compare(next1, next2);
                    if (compare != 0) {
                        return compare;
                    }
                }
                if (iterator2.hasNext()) {
                    return -1;
                }
                return 0;
            }
        };
    }

    protected static Comparator<ItemStack> itemStackComparator = new Comparator<ItemStack>() {
        @Override
        public int compare(ItemStack o1, ItemStack o2) {
            if (o1 == o2) return 0;
            MaterialData o1Data = o1.getData();
            MaterialData o2Data = o2.getData();
            int materialDataDiff = materialDataComparator.compare(o1Data, o2Data);
            if (materialDataDiff != 0) {
                return materialDataDiff;
            }
            return o2.getAmount() - o1.getAmount();
        }
    };

    protected static MaterialData normalize(MaterialData data) {
        if (data == null) return null;
        if (data.getData() == -1 || data.getItemType().getMaxDurability() > 0) {
            return new MaterialData(data.getItemTypeId(), (byte) 0);
        }
        return data;
    }
}
