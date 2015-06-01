package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.command.CommandSender;

public interface BrowserScreenFactory {
    BrowserScreen newBrowserScreen(int page, CommandSender player);
}
