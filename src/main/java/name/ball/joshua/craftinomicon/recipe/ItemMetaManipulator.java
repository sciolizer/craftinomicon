package name.ball.joshua.craftinomicon.recipe;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

// because sometimes itemMeta can be null, although I have no idea how
public class ItemMetaManipulator {

//    @MultiGauge(graph = "Null item meta") private MultiGaugeStat nullItemMetaStat;

    public Manipulator forItemStack(final ItemStack itemStack) {
        final ItemMeta itemMeta = getItemMeta(itemStack);
        if (itemMeta == null) {
            return new Manipulator() {
                @Override
                public Optional<String> getDisplayName() {
                    return Optional.absent();
                }

                @Override
                public void setDisplayName(String displayName) {
                }

                @Override
                public <T> T manipulate(Manipulation<T> manipulation) {
                    return manipulation.manipulate(new ManipulableItemMeta() {
                        @Override
                        public void setDisplayName(String displayName) {
                        }

                        @Override
                        public void addLore(List<String> lore) {
                        }
                    });
                }
            };
        }
        return new Manipulator() {
            @Override
            public Optional<String> getDisplayName() {
                return itemMeta.hasDisplayName() ? Optional.fromNullable(itemMeta.getDisplayName()) : Optional.<String>absent();
            }

            @Override
            public void setDisplayName(final String displayName) {
                manipulate(new Manipulation<Void>() {
                    @Override
                    public Void manipulate(ManipulableItemMeta itemMeta) {
                        itemMeta.setDisplayName(displayName);
                        return null;
                    }
                });
            }

            @Override
            public <T> T manipulate(Manipulation<T> manipulation) {
                T result = manipulation.manipulate(new ManipulableItemMeta() {

                    @Override
                    public void setDisplayName(final String displayName) {
                        itemMeta.setDisplayName(displayName);
                    }

                    @Override
                    public void addLore(final List<String> lore) {
                        List<String> tmpLore = itemMeta.getLore();
                        if (tmpLore == null) tmpLore = new ArrayList<String>(lore.size());
                        tmpLore.addAll(lore);
                        itemMeta.setLore(tmpLore);
                    }

                });
                itemStack.setItemMeta(itemMeta);
                return result;
            }
        };
    }

    private ItemMeta getItemMeta(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType()); // some plugins might be overriding the default behavior of getItemMeta()?!
            if (itemMeta == null) {
                MaterialData materialData = itemStack.getData();
                String dataID = materialData == null ? "null" : String.valueOf(materialData.getData());
//                nullItemMetaStat.set("material=" + itemStack.getTypeId() + ";data=" + dataID, 1);
            }
        }
        return itemMeta;
    }

    public interface Manipulator {
        Optional<String> getDisplayName();
        void setDisplayName(String displayName);
        <T> T manipulate(Manipulation<T> manipulation);
    }

    public interface Manipulation<T> {
        T manipulate(ManipulableItemMeta itemMeta);
    }

    public interface ManipulableItemMeta {
        void setDisplayName(String displayName);
        void addLore(List<String> lore);
    }

}
