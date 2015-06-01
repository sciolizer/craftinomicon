package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.InitializingBean;
import name.ball.joshua.craftinomicon.di.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

public class BrowserScreen implements Screen, InitializingBean {

    @Inject private BrowserScreenFactory browserScreenFactory;
    @Inject private RecipeMenuItems recipeMenuItems;
    @Inject private MenuUtilsFactory menuUtilsFactory;
    @Inject private RecipeSnapshot recipeSnapshot;
    @Inject private UpdateChecker updateChecker;

    private int page;
    private int numPages;
    private CommandSender player;

    public BrowserScreen(int page, CommandSender player) {
        this.page = page;
        this.player = player;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int size = recipeSnapshot.getAllMaterialsInAtLeastOneRecipe().size();
        numPages = (size - 1) / ITEMS_PER_PAGE + 1;
        while (page < 0) {
            page = page + numPages;
        }
        this.page = page % numPages;
    }

    @Override
    public void populate(final Menu menu) {
        menu.clear();

        int offset = page * ITEMS_PER_PAGE;
        int i = 0;
        // todo: use submap feature of sortedmap instead of iterating through each time
        for (MaterialData materialData : recipeSnapshot.getAllMaterialsInAtLeastOneRecipe()) {
            if (i >= offset) {
                int index = i - offset;
                menu.setMenuItem(index, recipeMenuItems.getRecipeMenuItem(materialData.toItemStack(1)));
            }
            i++;
            if (i >= offset + ITEMS_PER_PAGE) {
                break;
            }
        }

        MenuUtils menuUtils = menuUtilsFactory.newMenuUtils(menu);
        menuUtils.addNavigators(page == 0 ? null : browserScreenFactory.newBrowserScreen(page - 1, player), page >= numPages - 1 ? null : browserScreenFactory.newBrowserScreen(page + 1, player));

        List<String> updateText = updateChecker.getUpdateText(player);
        if (updateText != null) {
            ItemStack sign = menuUtils.sign("New version of craftinomicon available! Click to see link.");
            if (updateText.size() > 0) {
                ItemMeta itemMeta = sign.getItemMeta();
                List<String> lore = itemMeta.getLore();
                if (lore == null) lore = new ArrayList<String>();
                lore.addAll(updateText);
                itemMeta.setLore(lore);
                sign.setItemMeta(itemMeta);
            }
            menu.setMenuItem(49, new RotationlessMenuItem(sign) {
                @Override
                public void onInventoryClick(MenuItemClickEvent menuItemClickEvent) {
                    player.sendMessage("http://dev.bukkit.org/bukkit-plugins/craftinomicon/");
                }
            });
        }
    }

    private static final int ITEMS_PER_PAGE = 45;

}
