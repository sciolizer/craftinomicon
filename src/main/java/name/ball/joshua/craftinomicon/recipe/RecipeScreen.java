package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.Inject;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeScreen implements Screen {

    @Inject private MaterialDataSubstitutes materialDataSubstitutes;
    @Inject private MenuUtilsFactory menuUtilsFactory;
    @Inject private RecipeMenuItems recipeMenuItems;
    @Inject private RecipeScreenFactory recipeScreenFactory;

    protected List<MaterialRecipes.IconifiedRecipe> recipes;
    protected int offset;

    public RecipeScreen(List<MaterialRecipes.IconifiedRecipe> recipes, int offset) {
        this.recipes = recipes;
        while (offset < 0) {
            offset += recipes.size();
        }
        this.offset = offset % recipes.size();
    }

    @Override
    public void populate(final Menu menu) {
        menu.clear();

        Recipe recipe = this.recipes.get(this.offset).getRecipe();

        menu.setMenuItem(15, recipeMenuItems.getRecipeMenuItem(recipe.getResult()));

        final ItemStack[] typeItem = new ItemStack[1];

        RecipeAcceptor.accept(recipe, new RecipeVisitor<Void>() {
            @Override
            public Void visit(ShapedRecipe shapedRecipe) {
                typeItem[0] = new ItemStack(Material.WORKBENCH, 1);
                int row = 0;
                String[] sh = shapedRecipe.getShape();
                if (sh.length == 1) row = 1;
                for (String shape : sh) {
                    int col = 0;
                    char[] chars = shape.toCharArray();
                    if (chars.length == 1) {
                        col = 1;
                    }
                    for (char c : chars) {
                        ItemStack ingredient = shapedRecipe.getIngredientMap().get(c);
                        if (ingredient != null) {
                            setMenuItem(menu, SHAPED_RECIPE_SLOTS[row][col], ingredient);
                        }
                        col++;
                    }
                    row++;
                }
                return null;
            }

            @Override
            public Void visit(ShapelessRecipe shapelessRecipe) {
                typeItem[0] = new ItemStack(Material.WORKBENCH, 1);
                int i = 0;
                for (ItemStack ingredient : shapelessRecipe.getIngredientList()) {
                    setMenuItem(menu, SHAPELESS_RECIPE_SLOTS[i], ingredient);
                    i++;
                }
                return null;
            }

            @Override
            public Void visit(FurnaceRecipe furnaceRecipe) {
                typeItem[0] = new ItemStack(Material.FURNACE, 1);

                setMenuItem(menu, 12, furnaceRecipe.getInput());
                return null;
            }

            @Override
            public Void visitOther(Recipe recipe) {
                return null;
            }
        });

        menu.setMenuItem(30, new UnclickableMenuItem(typeItem[0]));

        final MenuUtils menuUtils = menuUtilsFactory.newMenuUtils(menu);
        if (recipes.size() > 1) {
            menuUtils.addNavigators(offset == 0 ? null : adjacent(offset - 1), offset >= recipes.size() - 1 ? null : adjacent(offset + 1));
        }
        if (menu.historySize() > 1) {
            menu.setMenuItem(0, new RotationlessMenuItem(menuUtils.sign("Back")) {
                @Override
                public void onInventoryClick(MenuItemClickEvent menuItemClickEvent) {
                    menuItemClickEvent.getMenu().pop();
                }
            });
        }

        showIndices(menu);
    }

    private void setMenuItem(Menu menu, int slot, ItemStack stack) {
        List<MaterialData> materialDatas = materialDataSubstitutes.get(stack.getData());
        final List<ItemStack> itemStacks = new ArrayList<ItemStack>(materialDatas.size());
        for (MaterialData data : materialDatas) {
            itemStacks.add(data.toItemStack(stack.getAmount()));
        }
        menu.setMenuItem(slot, recipeMenuItems.getRecipeMenuItem(itemStacks));
    }

    private void showIndices(final Menu menu) {
        int size = recipes.size();
        if (size <= 1) return;
        int firstColumn;
        int firstRecipe;
        if (size <= 7) {
            firstColumn = 1 + (7 - size) / 2;
            firstRecipe = 0;
        } else {
            firstColumn = 1;
            int currentPage = offset / 7;
            firstRecipe = currentPage * 7;
        }
        for (int i = firstRecipe; i < recipes.size() && i < firstRecipe + 7; i++) {
            final int j = i;
            int column = firstColumn + i - firstRecipe;
            menu.setMenuItem(45 + column, new MenuItem() {
                @Override
                public List<ItemStack> getItemStackRotation() {
                    return Collections.singletonList(recipes.get(j).getIcon());
                }

                @Override
                public void onInventoryClick(MenuItemClickEvent menuItemClickEvent) {
                    recipeScreenFactory.newRecipeScreen(recipes, j).populate(menu);
                }
            });
            if (i == offset) {
                menu.setMenuItem(36 + column, new UnclickableMenuItem(new ItemStack(Material.REDSTONE_TORCH_ON)));
            }
        }
    }

    private Screen adjacent(int offset) {
        return recipeScreenFactory.newRecipeScreen(recipes, offset);
    }

    private static final int[][] SHAPED_RECIPE_SLOTS = new int[][]{new int[]{2, 3, 4}, new int[]{11, 12, 13}, new int[]{20, 21, 22}};

    private static final int[] SHAPELESS_RECIPE_SLOTS = new int[] { 12, 3, 21, 11, 13, 2, 4, 20, 22 };
}
