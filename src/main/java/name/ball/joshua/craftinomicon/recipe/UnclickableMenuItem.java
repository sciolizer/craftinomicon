package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.inventory.ItemStack;

public class UnclickableMenuItem extends RotationlessMenuItem {

    public UnclickableMenuItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public void onInventoryClick(InventoryClickHandler.MenuItemClickEvent menuItemClickEvent) {
    }
}
