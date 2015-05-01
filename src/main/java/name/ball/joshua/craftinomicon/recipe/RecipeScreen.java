package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.List;

public class RecipeScreen implements Screen {

    protected ScreenUtilsFactory screenUtilsFactory;
    protected RecipeScreenFactory recipeScreenFactory;
    protected RecipeSnapshot recipeSnapshot;
    protected List<Recipe> recipes;
    protected int offset;

    public RecipeScreen(ScreenUtilsFactory screenUtilsFactory, RecipeScreenFactory recipeScreenFactory, RecipeSnapshot recipeSnapshot, List<Recipe> recipes, int offset) {
        this.screenUtilsFactory = screenUtilsFactory;
        this.recipeScreenFactory = recipeScreenFactory;
        this.recipeSnapshot = recipeSnapshot;
        this.recipes = recipes;
        while (offset < 0) {
            offset += recipes.size();
        }
        this.offset = offset % recipes.size();
    }

    @Override
    public void populate(final Menu menu) {
        menu.clear();

        Recipe recipe = getRecipe();

        menu.setMenuItem(15, recipe.getResult(), getRecipeScreen(menu, recipe.getResult().getData()));

        final int[] shapedRow = rowFor(ShapedRecipe.class);
        final int[] shapelessRow = rowFor(ShapelessRecipe.class);
        final int[] furnaceRow = rowFor(FurnaceRecipe.class);

        RecipeAcceptor.accept(recipe, new RecipeVisitor<Void>() {
            @Override
            public Void visit(ShapedRecipe shapedRecipe) {
                shapedRow[0] = 5;

                int row = 0;
                for (String shape : shapedRecipe.getShape()) {
                    int col = 0;
                    for (char c : shape.toCharArray()) {
                        ItemStack ingredient = shapedRecipe.getIngredientMap().get(c);
                        if (ingredient != null) {
                            menu.setMenuItem(SHAPED_RECIPE_SLOTS[row][col], ingredient, getRecipeScreen(menu, ingredient.getData()));
                        }
                        col++;
                    }
                    row++;
                }
                return null;
            }

            @Override
            public Void visit(ShapelessRecipe shapelessRecipe) {
                shapelessRow[0] = 5;

                int i = 0;
                for (ItemStack ingredient : shapelessRecipe.getIngredientList()) {
                    menu.setMenuItem(SHAPELESS_RECIPE_SLOTS[i], ingredient, getRecipeScreen(menu, ingredient.getData()));
                    i++;
                }
                return null;
            }

            @Override
            public Void visit(FurnaceRecipe furnaceRecipe) {
                furnaceRow[0] = 5;

                int inputColumn = 2;

                menu.setMenuItem(inputColumn, furnaceRecipe.getInput(), getRecipeScreen(menu, furnaceRecipe.getResult().getData()));

                menu.setMenuItem(inputColumn + 9, new ItemStack(Material.FIRE), Menu.MenuItemClickHandler.NULL);
                menu.setMenuItem(inputColumn + 18, new ItemStack(Material.COAL), Menu.MenuItemClickHandler.NULL);
                return null;
            }

            @Override
            public Void visitOther(Recipe recipe) {
                return null;
            }
        });

        ScreenUtils screenUtils = screenUtilsFactory.newScreenUtils(menu);
        if (recipes.size() > 1) {
            screenUtils.addNavigators(adjacent(offset - 1), adjacent(offset + 1));
        }
        if (menu.historySize() > 1) {
            menu.setMenuItem(0, screenUtils.sign("Back"), new Menu.MenuItemClickHandler() {
                @Override
                public void onInventoryClick(InventoryClickEvent inventoryClickEvent, ItemStack currentItemStack) {
                    menu.pop();
                }
            });
        }

        placeCrumb(menu, ShapedRecipe.class, Material.WORKBENCH, shapedRow[0], 3, "Shaped");
        placeCrumb(menu, ShapelessRecipe.class, Material.WORKBENCH, shapelessRow[0], 4, "Shapeless");
        placeCrumb(menu, FurnaceRecipe.class, Material.FURNACE, furnaceRow[0], 5, "Furnace");
    }

    protected void placeCrumb(final Menu menu, final Class<? extends Recipe> recipeType, Material material, int row, int col, String displayText) {
        if (row == -1) return;

        ItemStack indexStack;
        if (row == 5) {
            indexStack = itemStack(Material.REDSTONE_TORCH_ON, offset - indexOf(recipeType) + 1, "Current " + displayText + " Recipe");
            menu.setMenuItem(36 + col, indexStack, Menu.MenuItemClickHandler.NULL);
        }

        ItemStack totalStack = itemStack(material, countOf(recipeType), "Total " + displayText + " Recipes");

        int i = 45 + col;
        menu.setMenuItem(i, totalStack, new Menu.MenuItemClickHandler() {
            @Override
            public void onInventoryClick(InventoryClickEvent inventoryClickEvent, ItemStack currentItemStack) {
                recipeScreenFactory.newRecipeScreen(recipes, indexOf(recipeType)).populate(menu);
            }
        });
    }

    protected ItemStack itemStack(Material material, int count, String displayText) {
        ItemStack itemStack = new ItemStack(material, count);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayText);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    protected int[] rowFor(Class<? extends Recipe> recipeType) {
        return new int[] { indexOf(recipeType) >= 0 ? 6 : -1 };
    }

    protected int indexOf(Class<? extends Recipe> recipeType) {
        int i = 0;
        for (Recipe recipe : this.recipes) {
            if (recipeType.isAssignableFrom(recipe.getClass())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    protected int countOf(Class<? extends Recipe> recipeType) {
        int count = 0;
        for (Recipe recipe : this.recipes) {
            if (recipeType.isAssignableFrom(recipe.getClass())) {
                count++;
            }
        }
        return count;
    }

    protected Recipe getRecipe() {
        return this.recipes.get(this.offset);
    }

    protected Menu.MenuItemClickHandler getRecipeScreen(Menu menu, MaterialData materialData) {
        return screenUtilsFactory.newScreenUtils(menu).getRecipeClickHandler(materialData);
    }

    protected Screen adjacent(int offset) {
        return recipeScreenFactory.newRecipeScreen(recipes, offset);
    }

    protected static final int[][] SHAPED_RECIPE_SLOTS = new int[][]{new int[]{2, 3, 4}, new int[]{11, 12, 13}, new int[]{20, 21, 22}};

    protected static final int[] SHAPELESS_RECIPE_SLOTS = flatten(SHAPED_RECIPE_SLOTS);

    protected static int[] flatten(int[][] ns) {
        int[] result = new int[9];
        for (int i = 0; i < 9; i++) {
            result[i] = ns[i / 3][i % 3];
        }
        return result;
    }
}
