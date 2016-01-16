package name.ball.joshua.craftinomicon.recipe.i18n;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class TitleTranslationProvider implements Locales {

    private Set<String> titles = new LinkedHashSet<String>();

    public TitleTranslationProvider() {
        for (Locale locale : SUPPORTED_LOCALES) {
            ResourceBundle bundle = ResourceBundle.getBundle(MessageProvider.MESSAGE_BUNDLE, locale, new UTF8Control());
            titles.add(bundle.getString("title"));
        }
    }

    public Set<String> getPossibleTitles() {
        return titles;
    }
}
