package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.Inject;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RecipeMenuItems {

    @Inject private RecipeSnapshot recipeSnapshot;
    @Inject private RecipeScreenFactory recipeScreenFactory;

    public MenuItem getRecipeMenuItem(ItemStack itemStack) {
        return getRecipeMenuItem(Collections.singletonList(itemStack));
    }

    public MenuItem getRecipeMenuItem(List<ItemStack> unlorifiedDataStacks) {
        final List<ItemStack> itemStacks = new ArrayList<ItemStack>(unlorifiedDataStacks.size()); // todo: we need to make a copy of EACH ITEM in this list, because the item stacks might be coming from a place where they already have some lore of some kind
        for (ItemStack unlorifiedDataStack : unlorifiedDataStacks) {
            itemStacks.add(attachLore(unlorifiedDataStack));
        }
        ItemStack firstItemStack = itemStacks.get(0);
        if (getMaterialRecipes(firstItemStack) == null) {
            return new UnclickableMenuItem(firstItemStack);
        }
        return new MenuItem() {
            @Override
            public List<ItemStack> getItemStackRotation() {
                return itemStacks;
            }

            @Override
            public void onInventoryClick(InventoryClickHandler.MenuItemClickEvent menuItemClickEvent) {
                final MaterialRecipes materialRecipes = getMaterialRecipes(menuItemClickEvent.getInventoryClickEvent().getCurrentItem());
                Set<MaterialRecipes.IconifiedRecipe> recipes = null;
                InventoryClickEvent inventoryClickEvent = menuItemClickEvent.getInventoryClickEvent();
                if (inventoryClickEvent.isLeftClick()) {
                    if (!materialRecipes.getRecipes().isEmpty()) {
                        recipes = materialRecipes.getRecipesWithIcons();
                    }
                } else if (inventoryClickEvent.isRightClick()) {
                    if (!materialRecipes.getUsages().isEmpty()) {
                        recipes = materialRecipes.getUsagesWithIcons();
                    }
                }
                if (recipes != null) {
                    Menu menu = menuItemClickEvent.getMenu();
                    menu.dupe();
                    recipeScreenFactory.newRecipeScreen(new ArrayList<MaterialRecipes.IconifiedRecipe>(recipes), 0).populate(menu);
                }
            }
        };
    }

    private MaterialRecipes getMaterialRecipes(ItemStack stack) {
        final MaterialData materialData = RecipeSnapshot.normalize(stack.getData());
        return recipeSnapshot.getMaterialRecipes(materialData);
    }

    private ItemStack attachLore(ItemStack itemStack) {
        MaterialRecipes materialRecipes = recipeSnapshot.getMaterialRecipes(itemStack.getData());
        List<String> lore = new ArrayList<String>(2);
        int numRecipes = materialRecipes.getRecipes().size();
        if (numRecipes > 0) {
            lore.add(numRecipes + " " + pluralize("recipe", numRecipes) + " (Left-click)");
        }
        int numUsages = materialRecipes.getUsages().size();
        if (numUsages > 0) {
            lore.add(numUsages + " " + pluralize("usage", numUsages) + " (Right-click)");
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        // todo: sometimes itemMeta can be null?!
        itemMeta.setLore(lore);
        ItemStack lorifiedItemStack = itemStack.clone();
        lorifiedItemStack.setItemMeta(itemMeta);
        return lorifiedItemStack;
    }

    private String pluralize(String word, int num) {
        return word + (num == 1 ? "" : "s");
    }

}
