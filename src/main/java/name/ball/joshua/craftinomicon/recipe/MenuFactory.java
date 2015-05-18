package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.entity.HumanEntity;

public interface MenuFactory {
    Menu newMenu(HumanEntity humanEntity, int size, String title, InventoryClickHandler defaultInventoryClickHandler);
}
