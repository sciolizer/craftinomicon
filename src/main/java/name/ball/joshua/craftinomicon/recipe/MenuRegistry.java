package name.ball.joshua.craftinomicon.recipe;

import name.ball.joshua.craftinomicon.di.Inject;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.WeakHashMap;

public class MenuRegistry implements Listener {

    @Inject private Plugin plugin;
    @Inject private MenuFactory menuFactory;

    private final WeakHashMap<HumanEntity,Menu> menus = new WeakHashMap<HumanEntity, Menu>();
    private boolean timerRunning = false;

    public Menu newMenu(HumanEntity humanEntity, int size, String title, InventoryClickHandler defautInventoryClickHandler) {
        Menu menu = menuFactory.newMenu(humanEntity, size, title, defautInventoryClickHandler);
        menus.put(humanEntity, menu);
        startTimer();
        return menu;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        Menu menu = menus.get(holder);
        if (menu != null) {
            menu.onInventoryClick(event);
        }
    }

    private void startTimer() {
        if (timerRunning) return;
        timerRunning = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (menus.isEmpty()) {
                    this.cancel();
                    timerRunning = false;
                    return;
                }
                for (Menu menu : menus.values()) {
                    menu.rotateSubstitutables();
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

}
