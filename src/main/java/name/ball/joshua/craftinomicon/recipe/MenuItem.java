package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface MenuItem extends InventoryClickHandler {
    List<ItemStack> getItemStackRotation();
}
