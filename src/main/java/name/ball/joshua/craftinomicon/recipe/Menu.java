package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Menu {

    private HumanEntity humanEntity;
    private Inventory inventory;
    private List<List<MenuItem>> menuItemsHistory;
    private InventoryClickHandler defaultInventoryClickHandler;

    public Menu(HumanEntity humanEntity, int size, String title, InventoryClickHandler defaultInventoryClickHandler) {
        this.humanEntity = humanEntity;
        this.defaultInventoryClickHandler = defaultInventoryClickHandler;
        this.inventory = Bukkit.createInventory(humanEntity, size, title);
        List<MenuItem> mostRecent = new ArrayList<MenuItem>(size);
        for (int i = 0; i < size; i++) {
            mostRecent.add(null);
        }
        this.menuItemsHistory = new ArrayList<List<MenuItem>>();
        this.menuItemsHistory.add(mostRecent);
    }

    public int historySize() {
        return this.menuItemsHistory.size();
    }

    public void dupe() {
        List<MenuItem> duplicate = new ArrayList<MenuItem>(getMenuItems());
        this.menuItemsHistory.add(duplicate);
    }

    public boolean pop() {
        if (this.menuItemsHistory.size() <= 1) return false;
        this.menuItemsHistory.remove(this.menuItemsHistory.size() - 1);
        List<MenuItem> menuItems = getMenuItems();
        for (int i = 0; i < menuItems.size(); i++) {
            setMenuItem(i, menuItems.get(i));
        }
        return true;
    }

    private List<MenuItem> getMenuItems() {
        return this.menuItemsHistory.get(this.menuItemsHistory.size() - 1);
    }

    public void clear() {
        List<MenuItem> menuItems = getMenuItems();
        for (int i = 0; i < menuItems.size(); i++) {
            setMenuItem(i, null);
        }
    }

    public void setMenuItem(int i, MenuItem menuItem) {
        inventory.setItem(i, randomItemStack(menuItem));
        getMenuItems().set(i, menuItem);
    }

    private static Random random = new Random();

    // todo: separate recipe book recipe from ordinary book recipe
    // todo: disallow the use of a recipe book as a crafting ingredient

    public void open() {
        humanEntity.openInventory(inventory);
    }

    // NOT an @EventHandler; the real event handler is in MenuFactory
    public void onInventoryClick(final InventoryClickEvent inventoryClickEvent) {
        if (this.inventory.equals(inventoryClickEvent.getInventory())) {
            inventoryClickEvent.setCancelled(true);
            int slot = inventoryClickEvent.getRawSlot();
            List<MenuItem> menuItems = getMenuItems();
            InventoryClickHandler.MenuItemClickEvent menuItemClickEvent = new InventoryClickHandler.MenuItemClickEvent() {
                @Override
                public Menu getMenu() {
                    return Menu.this;
                }

                @Override
                public InventoryClickEvent getInventoryClickEvent() {
                    return inventoryClickEvent;
                }
            };
            if (slot >= 0 && slot < menuItems.size()) {
                MenuItem menuItem = menuItems.get(slot);
                if (menuItem != null) {
                    menuItem.onInventoryClick(menuItemClickEvent);
                }
            } else {
                defaultInventoryClickHandler.onInventoryClick(menuItemClickEvent);
            }
        }
    }

    private ItemStack randomItemStack(MenuItem menuItem) {
        if (menuItem == null) return null;
        List<ItemStack> itemStackRotation = menuItem.getItemStackRotation();
        return itemStackRotation.get(random.nextInt(itemStackRotation.size()));
    }

    public void rotateSubstitutables() {
        List<MenuItem> menuItems = getMenuItems();
        for (int i = 0; i < menuItems.size(); i++) {
            setMenuItem(i, menuItems.get(i));
        }
    }

}
