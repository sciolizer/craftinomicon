package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.Craftinomicon;
import name.ball.joshua.craftinomicon.di.Inject;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RecipeBrowser {

    @Inject private BrowserScreenFactory browserScreenFactory;
    @Inject private RecipeMenuItems recipeMenuItems;
    @Inject private MenuRegistry menuRegistry;

    // todo: ideally, we would sort recipes with specific inputs as earlier than recipes with flexible inputs
    // e.g. when looking at usages for jungle wood planks, show the recipe for jungle wood stairs before
    // showing the recipes that take arbitrary kinds of planks

    public void showAllItems(Player player) {
        final Menu[] menus = new Menu[1];
        menus[0] = menuRegistry.newMenu(player, 54, Craftinomicon.RECIPE_BOOK_DISPLAY_NAME, new InventoryClickHandler() {
            @Override
            public void onInventoryClick(MenuItemClickEvent menuItemClickEvent) {
                InventoryClickEvent inventoryClickEvent = menuItemClickEvent.getInventoryClickEvent();
                ItemStack currentItem = inventoryClickEvent.getCurrentItem();
                if (currentItem != null && !currentItem.getType().equals(Material.AIR)) {
                    recipeMenuItems.getRecipeMenuItem(currentItem).onInventoryClick(menuItemClickEvent);
                }
            }
        });
        BrowserScreen browserScreen = browserScreenFactory.newBrowserScreen(0);
        Menu menu = menus[0];
        browserScreen.populate(menu);
        menu.open();
    }

    // todo: brewing recipes
    // todo: text search (command line?)
    // todo: enchanting?

}
