package name.ball.joshua.craftinomicon.recipe.i18n;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LocalesTest implements Locales {

    @Test
    public void testGetAllLocales() throws Exception {
        List<Locale> allLocales = new ArrayList<Locale>(SUPPORTED_LOCALES);
        File[] files = new File("src/main/resources/i18n").listFiles();
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.startsWith("messages_") && fileName.endsWith(".properties")) {
                String lang = fileName.substring(9, 11);
                String country = fileName.substring(12, 14);
                Locale propertiesFileLocale = new Locale(lang, country);
                if (!allLocales.contains(propertiesFileLocale)) {
                    fail("Property file missing corresponding locale: " + lang + "_" + country);
                }
                allLocales.remove(propertiesFileLocale);
            }
        }
        assertEquals(1, allLocales.size());
        assertEquals(Locale.ENGLISH, allLocales.get(0));
    }
}
