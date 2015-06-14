package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.i18n.Translation;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RecipeBrowser {

    @Inject private BrowserScreenFactory browserScreenFactory;
    @Inject private RecipeMenuItems recipeMenuItems;
    @Inject private RecipeSnapshot recipeSnapshot;
    @Inject private MenuRegistry menuRegistry;
    @Translation(value = "title", english = "Craftinomicon") String titleTranslation;

    // todo: ideally, we would sort recipes with specific inputs as earlier than recipes with flexible inputs
    // e.g. when looking at usages for jungle wood planks, show the recipe for jungle wood stairs before
    // showing the recipes that take arbitrary kinds of planks

    public void showAllItems(HumanEntity player) {
        showScreen(player, browserScreenFactory.newBrowserScreen(0, player, recipeSnapshot.getAllMaterialsInAtLeastOneRecipe()));
    }

    public void showScreen(HumanEntity player, Screen screen) {
        final Menu[] menus = new Menu[1];
        menus[0] = menuRegistry.newMenu(player, 54, titleTranslation, new InventoryClickHandler() {
            @Override
            public void onInventoryClick(MenuItemClickEvent menuItemClickEvent) {
                InventoryClickEvent inventoryClickEvent = menuItemClickEvent.getInventoryClickEvent();
                ItemStack currentItem = inventoryClickEvent.getCurrentItem();
                if (currentItem != null && !currentItem.getType().equals(Material.AIR)) {
                    recipeMenuItems.getRecipeMenuItem(currentItem).onInventoryClick(menuItemClickEvent);
                }
            }
        });
        Menu menu = menus[0];
        screen.populate(menu);
        menu.open();
    }

    // todo: brewing recipes
    // todo: text search (command line?)
    // todo: enchanting?

}
