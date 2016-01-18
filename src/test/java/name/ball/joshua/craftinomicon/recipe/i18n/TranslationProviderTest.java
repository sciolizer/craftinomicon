package name.ball.joshua.craftinomicon.recipe.i18n;

import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TranslationProviderTest implements Locales {

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
        assertEquals("§7上一页", new MessageProvider(Locale.SIMPLIFIED_CHINESE).getMessage(String.class, "navigation.previous", "Previous"));
    }

    @Test
    public void testNumericSimplifiedChinese() throws Exception {
        assertEquals("§f1 §7合成方式 (左键查看)", new MessageProvider(Locale.SIMPLIFIED_CHINESE).getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)").getMessage(1));
    }

    @Test
    public void testEnglishSingular() throws Exception {
        assertEquals("1 recipe (Left-click)", new MessageProvider(Locale.ENGLISH).getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)").getMessage(1));
    }

    @Test
    public void testEnglishPlural() throws Exception {
        assertEquals("2 recipes (Left-click)", new MessageProvider(Locale.ENGLISH).getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)").getMessage(2));
    }

    @Test
    public void testPortugueseSingular() throws Exception {
        assertEquals("Receita com 1 clique (Botão esquerdo)", new MessageProvider(portugueseLocale).getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)").getMessage(1));
    }

    @Test
    public void testPortuguesePlural() throws Exception {
        assertEquals("2 receitas (Botão esquerdo)", new MessageProvider(portugueseLocale).getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)").getMessage(2));
    }

    @Test
    public void testRussian() throws Exception {
        MessageProvider russianMessageProvider = new MessageProvider(russianLocale);
        NumericTranslation translation = russianMessageProvider.getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)");
        assertEquals("1 рецепт (ЛКМ левый клик мыши)", translation.getMessage(1));
        assertEquals("21 рецепт (ЛКМ левый клик мыши)", translation.getMessage(21));
        assertEquals("2 рецепта (ЛКМ левый клик мыши)", translation.getMessage(2));
        assertEquals("0 рецептов (ЛКМ левый клик мыши)", translation.getMessage(0));
        // other is only for fractional values, I'm guessing, which we don't have in the plugin

        assertEquals("Назад", russianMessageProvider.getMessage(String.class, "navigation.back", "Back"));
    }

    @Test
    public void testGerman() throws Exception {
        MessageProvider germanMessageProvider = new MessageProvider(Locale.GERMANY);

        assertEquals("Zurück", germanMessageProvider.getMessage(String.class, "navigation.back", "Back"));

        NumericTranslation numRecipesTranslation = germanMessageProvider.getMessage(NumericTranslation.class, "recipe-click.num-recipes", "${num-recipes} recipes (Left-click)");
        assertEquals("1 Rezept (Linksklick)", numRecipesTranslation.getMessage(1));
        assertEquals("7 Rezepte (Linksklick)", numRecipesTranslation.getMessage(7));

        NumericTranslation numUsagesTranslation = germanMessageProvider.getMessage(NumericTranslation.class, "usage-click.num-usages", "${num-usages} usages (Right-click)");
        assertEquals("Einmalige Benutzung (Rechtsklick)", numUsagesTranslation.getMessage(1));
        assertEquals("Zweimalige Benutzung (Rechtsklick)", numUsagesTranslation.getMessage(2));
        assertEquals("7-malige Benutzung (Rechtsklick)", numUsagesTranslation.getMessage(7));
    }

    private List<Locale> getLocales() {
        return SUPPORTED_LOCALES;
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
