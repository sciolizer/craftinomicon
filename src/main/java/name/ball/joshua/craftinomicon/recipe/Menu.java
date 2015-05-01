package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.*;

public class Menu {

    protected Map<Material,SortedSet<MaterialData>> substitutables;
    protected HumanEntity humanEntity;
    protected Inventory inventory;
    protected List<List<MenuItem>> menuItemsHistory;
    protected MenuItemClickHandler playerMenuItemClickHandler;

    public Menu(Map<Material,SortedSet<MaterialData>> substitutables, HumanEntity humanEntity, int size, String title, MenuItemClickHandler playerMenuItemClickHandler) {
        this.substitutables = substitutables;
        this.humanEntity = humanEntity;
        this.playerMenuItemClickHandler = playerMenuItemClickHandler;
        this.inventory = Bukkit.createInventory(humanEntity, size, title);
        List<MenuItem> mostRecent = new ArrayList<MenuItem>(size);
        for (int i = 0; i < size; i++) {
            mostRecent.add(null);
        }
        this.menuItemsHistory = new ArrayList<List<MenuItem>>();
        this.menuItemsHistory.add(mostRecent);
    }

    public Inventory getInventory() {
        return inventory;
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
            MenuItem menuItem = menuItems.get(i);
            setItem(i, menuItem == null ? null : menuItem.rotatable);
        }
        return true;
    }

    protected List<MenuItem> getMenuItems() {
        return this.menuItemsHistory.get(this.menuItemsHistory.size() - 1);
    }

    public void clear() {
        List<MenuItem> menuItems = getMenuItems();
        for (int i = 0; i < menuItems.size(); i++) {
            setItem(i, null);
            menuItems.set(i, null);
        }
    }

    public void setMenuItem(int i, ItemStack itemStack, MenuItemClickHandler menuItemClickHandler) {
        setItem(i, itemStack);
        getMenuItems().set(i, new MenuItem(menuItemClickHandler, itemStack));
    }

    protected void setItem(int i, ItemStack stack) {
        inventory.setItem(i, randomSubstitute(stack));
    }

    protected static Random random = new Random();

    protected ItemStack randomSubstitute(ItemStack data) {
        if (data == null) return null;
        MaterialData data1 = data.getData();
        byte data2 = data1.getData();
        if (data2 != (byte)-1) return data;
        SortedSet<MaterialData> subst = substitutables.get(data.getType());
        if (subst == null) {
            return data;
        }
        int ind = random.nextInt(subst.size());
        MaterialData materialData = new ArrayList<MaterialData>(subst).get(ind);
        return materialData.toItemStack(data.getAmount());
    }

    // todo: separate recipe book recipe from ordinary book recipe
    // todo: disallow the use of a recipe book as a crafting ingredient

    public void open() {
        humanEntity.openInventory(inventory);
    }

    // NOT an @EventHandler; the real event handler is in MenuFactory
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        if (this.inventory.equals(inventoryClickEvent.getInventory())) {
            inventoryClickEvent.setCancelled(true);
            int slot = inventoryClickEvent.getRawSlot();
            List<MenuItem> menuItems = getMenuItems();
            if (slot >= 0 && slot < menuItems.size()) {
                MenuItem menuItem = menuItems.get(slot);
                if (menuItem != null) {
                    MenuItemClickHandler menuItemClickHandler = menuItem.clickHandler;
                    menuItemClickHandler.onInventoryClick(inventoryClickEvent, inventory.getItem(slot));
                }
            } else {
                playerMenuItemClickHandler.onInventoryClick(inventoryClickEvent, inventoryClickEvent.getCurrentItem());
            }
        }
    }

    public void rotateSubstitutables() {
        List<MenuItem> menuItems = getMenuItems();
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem menuItem = menuItems.get(i);
            if (menuItem != null && menuItem.rotatable != null) {
                setItem(i, menuItem.rotatable);
            }
        }
    }

    public static interface MenuItemClickHandler {
        void onInventoryClick(InventoryClickEvent inventoryClickEvent, ItemStack currentItemStack);
        public static final MenuItemClickHandler NULL = new MenuItemClickHandler() {
            @Override
            public void onInventoryClick(InventoryClickEvent inventoryClickEvent, ItemStack currentItemStack) {

            }
        };
    }

    protected static class MenuItem {
        public MenuItemClickHandler clickHandler;
        public ItemStack rotatable;

        public MenuItem(MenuItemClickHandler clickHandler, ItemStack rotatable) {
            this.clickHandler = clickHandler;
                this.rotatable = rotatable;
        }
    }

}
