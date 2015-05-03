package name.ball.joshua.craftinomicon;

import name.ball.joshua.craftinomicon.recipe.MaterialDataSubstitutes;
import name.ball.joshua.craftinomicon.recipe.MenuFactory;
import name.ball.joshua.craftinomicon.recipe.NEI;
import name.ball.joshua.craftinomicon.recipe.RecipeSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Craftinomicon extends JavaPlugin {

    protected NEI nei;

    public void onDisable() {
    }

    public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager();

        pm.registerEvents(new CraftinomiconTestRunner(), this);

        final ItemStack recipeBookItem = new ItemStack(Material.BOOK);
        ItemMeta itemMeta = recipeBookItem.getItemMeta();
        itemMeta.setDisplayName(RECIPE_BOOK_DISPLAY_NAME);
        recipeBookItem.setItemMeta(itemMeta);

        ShapelessRecipe recipeBookRecipe = new ShapelessRecipe(recipeBookItem);
        recipeBookRecipe.addIngredient(Material.BOOK);
        recipeBookRecipe.addIngredient(Material.WORKBENCH);

        final Server server = Bukkit.getServer();
        server.addRecipe(recipeBookRecipe);
        class RecipeBookCraftingInterceptor implements Listener {
            @EventHandler
            public void convertToRecipeBook(PrepareItemCraftEvent event) {
                Recipe recipe = event.getInventory().getRecipe();
                if (recipe instanceof ShapelessRecipe) {
                    ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                    List<ItemStack> ingredientList = shapelessRecipe.getIngredientList();
                    if (ingredientList.size() == 2 && ingredientList.get(1).getType().equals(Material.WORKBENCH)) {
                        ItemStack firstIngredient = ingredientList.get(0);
                        if (Material.BOOK.equals(firstIngredient.getType()) && !isRecipeBook(firstIngredient)) {
                            event.getInventory().setResult(recipeBookItem);
                        }
                    }
                }
            }
        }
        pm.registerEvents(new RecipeBookCraftingInterceptor(), this);

        class RecipeBookConsumeEventHandler implements Listener {
            @EventHandler
            public void onConsumeRecipeBook(PlayerInteractEvent event) {
                Action action = event.getAction();
                switch (action) {
                    case RIGHT_CLICK_AIR:
                    case RIGHT_CLICK_BLOCK:
                        ItemStack itemInHand = event.getPlayer().getItemInHand();
                        if (isRecipeBook(itemInHand)) {
                            nei.showAllItems(event.getPlayer());
                        }
                }
            }

        }
        pm.registerEvents(new RecipeBookConsumeEventHandler(), this);

        MenuFactory menuFactory = new MenuFactory(this);
        pm.registerEvents(menuFactory, this);
        this.nei = new NEI(menuFactory);

        // We don't actually want to compute the recipe list until a player action requires it, since other plugins
        // might not yet have had a chance to register their recipes. But doing a dry-run here will let us detect
        // bugs early. If the initialization fails, then bukkit will disable this plugin, and so players will
        // not be left with buggy craftinomicon books.
        new RecipeSnapshot(new MaterialDataSubstitutes()).initialize();
    }

    protected boolean isRecipeBook(ItemStack itemStack) {
        if (!Material.BOOK.equals(itemStack.getType()) || !itemStack.hasItemMeta()) return false;
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta.hasDisplayName() && RECIPE_BOOK_DISPLAY_NAME.equals(itemMeta.getDisplayName());
    }

    public static final String RECIPE_BOOK_DISPLAY_NAME = "Craftinomicon";

    // todo: make coal clickable?

}
