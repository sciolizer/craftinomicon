package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.SortedSet;

public class ScreenUtils {

    protected RecipeScreenFactory recipeScreenFactory;
    protected RecipeSnapshot recipeSnapshot;
    protected Menu menu;

    public ScreenUtils(RecipeScreenFactory recipeScreenFactory, RecipeSnapshot recipeSnapshot, Menu menu) {
        this.recipeScreenFactory = recipeScreenFactory;
        this.recipeSnapshot = recipeSnapshot;
        this.menu = menu;
    }

    public void addNavigators(Screen previous, Screen next) {
        if (previous != null) {
            addNavigator(45, "Previous", previous);
        }
        if (next != null) {
            addNavigator(53, "Next", next);
        }
    }

    protected void addNavigator(int slot, String displayText, final Screen screen) {
        ItemStack sign = sign(displayText);

        menu.setMenuItem(slot, sign, new Menu.MenuItemClickHandler() {
            @Override
            public void onInventoryClick(InventoryClickEvent inventoryClickEvent, ItemStack currentItemStack) {
                screen.populate(menu);
            }
        });
    }

    public ItemStack sign(String displayText) {
        ItemStack sign = new ItemStack(Material.SIGN);
        ItemMeta itemMeta = sign.getItemMeta();
        itemMeta.setDisplayName(displayText);
        sign.setItemMeta(itemMeta);
        return sign;
    }

    public Menu.MenuItemClickHandler getRecipeClickHandler(final MaterialData nextX) {
        MaterialData next = RecipeSnapshot.normalize(nextX);
        final MaterialRecipes materialRecipes = recipeSnapshot.snapshot.get(next);
        if (materialRecipes == null) {
            return Menu.MenuItemClickHandler.NULL;
        }
        return new Menu.MenuItemClickHandler() {
            @Override
            public void onInventoryClick(InventoryClickEvent inventoryClickEvent, ItemStack currentItemStack) {
                MaterialRecipes currentMaterialRecipes = recipeSnapshot.snapshot.get(RecipeSnapshot.normalize(currentItemStack.getData()));
                SortedSet<Recipe> recipes = null;
                if (inventoryClickEvent.isLeftClick()) {
                    if (!currentMaterialRecipes.recipes.isEmpty()) {
                        recipes = currentMaterialRecipes.recipes;
                    }
                } else if (inventoryClickEvent.isRightClick()) {
                    if (!currentMaterialRecipes.usages.isEmpty()) {
                        recipes = currentMaterialRecipes.usages;
                    }
                }
                if (recipes != null) {
                    menu.dupe();
                    recipeScreenFactory.newRecipeScreen(new ArrayList<Recipe>(recipes), 0).populate(menu);
                }
            }
        };
    }


}
