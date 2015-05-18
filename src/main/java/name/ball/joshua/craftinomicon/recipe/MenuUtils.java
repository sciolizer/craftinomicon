package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuUtils {

    private Menu menu;

    public MenuUtils(Menu menu) {
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
        ItemMeta itemMeta = sign.getItemMeta();
        itemMeta.setDisplayName(displayText);
        sign.setItemMeta(itemMeta);
        return sign;
    }


}
