package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface InventoryClickHandler {
    void onInventoryClick(MenuItemClickEvent menuItemClickEvent);

    interface MenuItemClickEvent {
        Menu getMenu();
        InventoryClickEvent getInventoryClickEvent();
    }
}
