package name.ball.joshua.craftinomicon.recipe.comparator;

import org.bukkit.material.MaterialData;

import java.util.Comparator;

public class MaterialDataComparator implements Comparator<MaterialData> {
    @Override
    public int compare(MaterialData o2, MaterialData o1) {
        if (o1 == o2) return 0;
        int itemTypeDiff = o2.getItemTypeId() - o1.getItemTypeId();
        if (itemTypeDiff != 0) {
            return itemTypeDiff;
        }
        return (int) o2.getData() - (int) o1.getData();
    }
}
