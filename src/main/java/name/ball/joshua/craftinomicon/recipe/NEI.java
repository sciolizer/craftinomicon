package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.Craftinomicon;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class NEI {

    protected MenuFactory menuFactory;
    protected RecipeSnapshot recipeSnapshot;

    public NEI(MenuFactory menuFactory) {
        this.menuFactory = menuFactory;
    }

    // todo: ideally, we would sort recipes with specific inputs as earlier than recipes with flexible inputs
    // e.g. when looking at usages for jungle wood planks, show the recipe for jungle wood stairs before
    // showing the recipes that take arbitrary kinds of planks

    public void showAllItems(Player player) {
        final RecipeScreenFactory[] recipeScreenFactories = new RecipeScreenFactory[1];
        final ScreenUtilsFactory[] screenUtilsFactories = new ScreenUtilsFactory[1];
        recipeScreenFactories[0] = new RecipeScreenFactory() {
            @Override
            public RecipeScreen newRecipeScreen(List<Recipe> recipes, int offset) {
                return new RecipeScreen(screenUtilsFactories[0], recipeScreenFactories[0], getAllRecipes(), recipes, offset);
            }
        };
        screenUtilsFactories[0] = new ScreenUtilsFactory() {
            @Override
            public ScreenUtils newScreenUtils(Menu menu) {
                return new ScreenUtils(recipeScreenFactories[0], recipeSnapshot, menu);
            }
        };
        final BrowserScreenFactory[] browserScreenFactories = new BrowserScreenFactory[1];
        browserScreenFactories[0] = new BrowserScreenFactory() {
            @Override
            public BrowserScreen newBrowserScreen(int page) {
                return new BrowserScreen(screenUtilsFactories[0], browserScreenFactories[0], getAllRecipes(), page);
            }
        };
        final Menu[] menus = new Menu[1];
        menus[0] = menuFactory.newMenu(player, 54, Craftinomicon.RECIPE_BOOK_DISPLAY_NAME, new Menu.MenuItemClickHandler() {
            @Override
            public void onInventoryClick(InventoryClickEvent inventoryClickEvent, ItemStack currentItemStack) {
                ItemStack currentItem = inventoryClickEvent.getCurrentItem();
                if (currentItem != null) {
                    screenUtilsFactories[0].newScreenUtils(menus[0]).getRecipeClickHandler(currentItemStack.getData()).onInventoryClick(inventoryClickEvent, currentItemStack);
                }
            }
        });
        BrowserScreenFactory browserScreenFactory = browserScreenFactories[0];
        BrowserScreen browserScreen = browserScreenFactory.newBrowserScreen(0);
        Menu menu = menus[0];
        browserScreen.populate(menu);
        menus[0].open();
    }

    // todo: brewing recipes
    // todo: text search (command line?)
    // todo: enchanting?

    protected RecipeSnapshot getAllRecipes() {
        if (recipeSnapshot == null) {
            recipeSnapshot = new RecipeSnapshot(new MaterialDataSubstitutes());
            recipeSnapshot.initialize();
        }
        return recipeSnapshot;
    }

    protected ItemStack sign(String displayName) {
        ItemStack stack = new ItemStack(Material.SIGN);
        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        stack.setItemMeta(itemMeta);
        return stack;
    }

}
