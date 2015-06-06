package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.i18n.Translation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MenuUtils {

    private Menu menu;

    @Inject private ItemMetaManipulator itemMetaManipulator;

    @Translation(value = "navigation.previous", english = "Previous") String previousTranslation;
    @Translation(value = "navigation.next", english = "Next") String nextTranslation;

    public MenuUtils(Menu menu) {
        this.menu = menu;
    }

    public void addNavigators(Screen previous, Screen next) {
        if (previous != null) {
            addNavigator(45, previousTranslation, previous);
        }
        if (next != null) {
            addNavigator(53, nextTranslation, next);
        }
    }

    private void addNavigator(int slot, String displayText, final Screen screen) {
        menu.setMenuItem(slot, new RotationlessMenuItem(sign(displayText)) {
            @Override
            public void onInventoryClick(MenuItemClickEvent menuItemClickEvent) {
                screen.populate(menu);
            }
        });
    }

    public ItemStack sign(String displayText) {
        ItemStack sign = new ItemStack(Material.SIGN);
        itemMetaManipulator.forItemStack(sign).setDisplayName(displayText);
        return sign;
    }


}
