package name.ball.joshua.craftinomicon.recipe.i18n;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public interface Locales {

    static final Locale portugueseLocale = new Locale("pt", "BR");

    static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(portugueseLocale, Locale.ENGLISH, Locale.TRADITIONAL_CHINESE, Locale.SIMPLIFIED_CHINESE);;

}
