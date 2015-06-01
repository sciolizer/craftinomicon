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

    private int page;
    private int numPages;

    public BrowserScreen(int page) {
        this.page = page;
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

        menuUtilsFactory.newMenuUtils(menu).addNavigators(page == 0 ? null : browserScreenFactory.newBrowserScreen(page - 1), page >= numPages - 1 ? null : browserScreenFactory.newBrowserScreen(page + 1));
    }

    private static final int ITEMS_PER_PAGE = 45;

}
