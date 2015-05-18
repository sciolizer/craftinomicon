package name.ball.joshua.craftinomicon.recipe.comparator;

import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.IngredientsGetter;
import name.ball.joshua.craftinomicon.recipe.RecipeAcceptor;
import name.ball.joshua.craftinomicon.recipe.RecipeVisitor;
import org.bukkit.inventory.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

public class RecipeComparator implements Comparator<Recipe> {

    @Inject protected IngredientsGetter ingredientsGetter;
    @Inject protected MaterialDataComparator materialDataComparator;
    @Inject protected ItemStackComparator itemStackComparator;

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
        SortedSet<ItemStack> ingredients1 = ingredientsGetter.getIngredients(o1);
        SortedSet<ItemStack> ingredients2 = ingredientsGetter.getIngredients(o2);
        return iterableComparator(itemStackComparator).compare(ingredients1, ingredients2);
    }

    protected <T> Comparator<Iterable<T>> iterableComparator(final Comparator<T> innerComparator) {
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

    protected RecipeVisitor<Integer> recipeTypeIndexVisitor = new RecipeVisitor<Integer>() {
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


}
