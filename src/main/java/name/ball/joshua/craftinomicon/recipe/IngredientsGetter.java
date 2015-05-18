package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.comparator.ItemStackComparator;
import org.bukkit.inventory.*;

import java.util.SortedSet;
import java.util.TreeSet;

public class IngredientsGetter {

    @Inject private ItemStackComparator itemStackComparator;

    private RecipeVisitor<SortedSet<ItemStack>> ingredientsVisitor = new RecipeVisitor<SortedSet<ItemStack>>() {
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

    public SortedSet<ItemStack> getIngredients(Recipe recipe) {
        return RecipeAcceptor.accept(recipe, ingredientsVisitor);
    }

}
