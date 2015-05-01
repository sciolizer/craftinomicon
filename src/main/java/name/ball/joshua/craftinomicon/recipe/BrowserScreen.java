package name.ball.joshua.craftinomicon.recipe;

import org.bukkit.material.MaterialData;

import java.util.Map;

public class BrowserScreen implements Screen {

    protected ScreenUtilsFactory screenUtilsFactory;
    protected BrowserScreenFactory browserScreenFactory;
    protected RecipeSnapshot recipeSnapshot;
    protected int page;

    public BrowserScreen(ScreenUtilsFactory screenUtilsFactory, BrowserScreenFactory browserScreenFactory, RecipeSnapshot recipeSnapshot, int page) {
        this.screenUtilsFactory = screenUtilsFactory;
        this.browserScreenFactory = browserScreenFactory;
        this.recipeSnapshot = recipeSnapshot;

        int size = recipeSnapshot.snapshot.size();
        int numPages = (size - 1) / 54 + 1;
        while (page < 0) {
            page = page + numPages;
        }
        this.page = page % numPages;
    }

    @Override
    public void populate(final Menu menu) {
        menu.clear();

        int offset = page * 54;
        int i = 0;
        // todo: use submap feature of sortedmap instead of iterating through each time
        for (final Map.Entry<MaterialData, MaterialRecipes> entry : recipeSnapshot.snapshot.entrySet()) {
            if (i >= offset) {
                int index = i - offset;
                menu.setMenuItem(index, entry.getKey().toItemStack(1), screenUtilsFactory.newScreenUtils(menu).getRecipeClickHandler(entry.getKey()));
            }
            i++;
            if (i >= offset + 45) {
                break;
            }
        }

        screenUtilsFactory.newScreenUtils(menu).addNavigators(browserScreenFactory.newBrowserScreen(page - 1), browserScreenFactory.newBrowserScreen(page + 1));
    }

}
