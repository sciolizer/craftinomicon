package name.ball.joshua.craftinomicon;

import com.google.common.base.Optional;
import name.ball.joshua.craftinomicon.di.Inject;
import name.ball.joshua.craftinomicon.recipe.ItemMetaManipulator;
import name.ball.joshua.craftinomicon.recipe.i18n.TitleTranslationProvider;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RecipeBookChecker {

    @Inject private ItemMetaManipulator itemMetaManipulator;
    @Inject private TitleTranslationProvider titleTranslationProvider;

    public boolean isRecipeBook(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (!Material.BOOK.equals(itemStack.getType())) return false;
        Optional<String> displayName = itemMetaManipulator.forItemStack(itemStack).getDisplayName();
        // It's possible that the Craftinomicon was crafted when the server was configured in a different
        // language, so we have to check if the display name of the book is ANY of the translations of
        // "Craftinomicon".
        return displayName.isPresent() && titleTranslationProvider.getPossibleTitles().contains(displayName.get());
    }

}
