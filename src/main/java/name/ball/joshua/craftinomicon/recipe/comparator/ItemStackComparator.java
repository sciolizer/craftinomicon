package name.ball.joshua.craftinomicon.recipe.comparator;

import name.ball.joshua.craftinomicon.di.Inject;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Comparator;

public class ItemStackComparator implements Comparator<ItemStack> {

    @Inject private MaterialDataComparator materialDataComparator;

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        if (o1 == o2) return 0;
        MaterialData o1Data = o1.getData();
        MaterialData o2Data = o2.getData();
        int materialDataDiff = materialDataComparator.compare(o1Data, o2Data);
        if (materialDataDiff != 0) {
            return materialDataDiff;
        }
        return o2.getAmount() - o1.getAmount();
    }
}
