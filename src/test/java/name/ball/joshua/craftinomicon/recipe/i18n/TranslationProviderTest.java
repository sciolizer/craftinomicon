package name.ball.joshua.craftinomicon.recipe.i18n;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TranslationProviderTest {

    @Test
    public void testAllLocalesHaveSameKeysAndOfSameType() throws Exception {
        MessageProvider englishMessageProvider = new MessageProvider(Locale.ENGLISH);
        for (Locale locale : getLocales()) {
            MessageProvider messageProvider = new MessageProvider(locale);
            if (messageProvider.translations.size() > englishMessageProvider.translations.size()) {
                fail("Locale " + locale + " has more keys than english locale");
            }
            for (Map.Entry<String, MessageProvider.Translation<?>> entry : messageProvider.translations.entrySet()) {
                if (!englishMessageProvider.translations.containsKey(entry.getKey())) {
                    fail("Locale " + locale + " contains the key " + entry.getKey() + " which does not appear in the english locale");
                }
                assertEquals(englishMessageProvider.translations.get(entry.getKey()).getType(), entry.getValue().getType());
            }
        }
    }

    @Test
    public void testChinesePreviousIsInChinese() throws Exception {
        assertEquals("上一個", new MessageProvider(Locale.TRADITIONAL_CHINESE).getMessage(String.class, "navigation.previous", "Previous"));
    }

    @Test
    public void testChineseTitleFallsBackToEnglishTitle() throws Exception {
        assertEquals("Craftinomicon", new MessageProvider(Locale.TRADITIONAL_CHINESE).getMessage(String.class, "title", "Craftinomicon"));
    }

    @Test
    public void testNumericChinese() throws Exception {
        assertEquals("1 說明書 (左鍵)", new MessageProvider(Locale.TRADITIONAL_CHINESE).getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)").getMessage(1));
    }

    @Test
    public void testSimplifiedPrevious() throws Exception {
        assertEquals("上一个", new MessageProvider(Locale.SIMPLIFIED_CHINESE).getMessage(String.class, "navigation.previous", "Previous"));
    }

    @Test
    public void testNumericSimplifiedChinese() throws Exception {
        assertEquals("1 说明书 (左键)", new MessageProvider(Locale.SIMPLIFIED_CHINESE).getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)").getMessage(1));
    }

    @Test
    public void testEnglishSingular() throws Exception {
        assertEquals("1 recipe (Left-click)", new MessageProvider(Locale.ENGLISH).getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)").getMessage(1));
    }

    @Test
    public void testEnglishPlural() throws Exception {
        assertEquals("2 recipes (Left-click)", new MessageProvider(Locale.ENGLISH).getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)").getMessage(2));
    }

    private List<Locale> getLocales() {
        return Arrays.asList(Locale.ENGLISH, Locale.TRADITIONAL_CHINESE, Locale.SIMPLIFIED_CHINESE);
    }

//    @Test
//    public void testAllLocalesMentionedInConfigYml() throws Exception {
//        URL resource = getClass().getClassLoader().getResource("messages.properties");
//        System.out.println("resource = " + resource);
//
//    }

    // todo: add a test which iterates through the available options listed in the comment in config.yml, and make sure
    // that none are left out
}
