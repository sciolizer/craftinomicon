package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.entity.HumanEntity;
import org.bukkit.material.MaterialData;

import java.util.Set;

public interface BrowserScreenFactory {
    BrowserScreen newBrowserScreen(int page, HumanEntity player, Set<MaterialData> stacks);
}
