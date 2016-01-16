package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.InitializingBean;
import name.ball.joshua.craftinomicon.di.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.List;

public class BrowserScreen implements Screen, InitializingBean {

    @Inject private BrowserScreenFactory browserScreenFactory;
    @Inject private ItemMetaManipulator itemMetaManipulator;
    @Inject private RecipeMenuItems recipeMenuItems;
    @Inject private MenuUtilsFactory menuUtilsFactory;
    @Inject private RecipeSnapshot recipeSnapshot;
    @Inject private UpdateChecker updateChecker;

    private int page;
    private int numPages;
    private HumanEntity player;

    public BrowserScreen(int page, HumanEntity player) {
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

        if (player instanceof CommandSender) {
            final List<String> updateText = updateChecker.getUpdateText(player);
            if (updateText != null) {
                ItemStack sign = menuUtils.sign("New version of craftinomicon available! Click to see link.");
                if (updateText.size() > 0) {
                    itemMetaManipulator.forItemStack(sign).manipulate(new ItemMetaManipulator.Manipulation<Void>() {
                        @Override
                        public Void manipulate(ItemMetaManipulator.ManipulableItemMeta itemMeta) {
                            itemMeta.addLore(updateText);
                            return null;
                        }
                    });
                }
                menu.setMenuItem(49, new RotationlessMenuItem(sign) {
                    @Override
                    public void onInventoryClick(MenuItemClickEvent menuItemClickEvent) {
                        ((CommandSender)player).sendMessage("http://dev.bukkit.org/bukkit-plugins/craftinomicon/");
                    }
                });
            }
        }
    }

    private static final int ITEMS_PER_PAGE = 45;

}
