package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public abstract class RotationlessMenuItem implements MenuItem {

    private final List<ItemStack> rotation;

    public RotationlessMenuItem(ItemStack stack) {
        this.rotation = Collections.singletonList(stack);
    }

    @Override
    public List<ItemStack> getItemStackRotation() {
        return rotation;
    }

}
