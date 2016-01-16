package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.entity.HumanEntity;

public interface BrowserScreenFactory {
    BrowserScreen newBrowserScreen(int page, HumanEntity player);
}
