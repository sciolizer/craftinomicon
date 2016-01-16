package name.ball.joshua.craftinomicon.recipe.i18n;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TitleTranslationProviderTest {

    @Test
    public void testGetPossibleTitles() throws Exception {
        Set<String> titles = new TitleTranslationProvider().getPossibleTitles();
        assertTrue(titles.contains("Craftinomicon"));
        assertTrue(titles.contains("Крафтономикон"));
    }

}
