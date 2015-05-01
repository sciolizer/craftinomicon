package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.SortedSet;
import java.util.WeakHashMap;

public class MenuFactory implements Listener {

    protected final WeakHashMap<HumanEntity,Menu> menus = new WeakHashMap<HumanEntity, Menu>();
    protected Map<Material,SortedSet<MaterialData>> substitutables;
    protected Plugin plugin;
    protected boolean timerRunning = false;

    public MenuFactory(Plugin plugin) {
        this.plugin = plugin;
    }

    public Menu newMenu(HumanEntity humanEntity, int size, String title, Menu.MenuItemClickHandler menuItemClickHandler) {
        Menu menu = new Menu(getSubstitutables(), humanEntity, size, title, menuItemClickHandler);
        menus.put(humanEntity, menu);
        startTimer();
        return menu;
    }

    protected Map<Material,SortedSet<MaterialData>> getSubstitutables() {
        if (substitutables == null) {
            substitutables = new MaterialDataSubstitutes().substitutes;
        }
        return substitutables;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        Menu menu = menus.get(holder);
        if (menu != null) {
            menu.onInventoryClick(event);
        }
    }

    protected void startTimer() {
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
