package name.ball.joshua.craftinomicon.recipe.i18n;

import java.util.*;

public class MessageProvider {

    private static final String NUMERIC_MNEMONIC_RESOLVER = "numeric.mnemonic.resolver";
    static final String MESSAGE_BUNDLE = "i18n/messages";

    protected Map<String,Translation<?>> english = new LinkedHashMap<String, Translation<?>>();
    protected Map<String,Translation<?>> translations = new LinkedHashMap<String, Translation<?>>();
    private NumericMnemonicResolver numericMnemonicResolver;

    public MessageProvider(Locale locale) {
        UnzippedResourceBundle englishBundle = unzip(ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH, new UTF8Control()));
        english = getTranslationMap(englishBundle, englishBundle);
        UnzippedResourceBundle bundle = unzip(ResourceBundle.getBundle(MESSAGE_BUNDLE, locale, new UTF8Control()));
        setNumericMnemonicResolver(bundle.numericMnemonicResolverClass);
        translations = getTranslationMap(englishBundle, bundle);
    }

    private Map<String, Translation<?>> getTranslationMap(UnzippedResourceBundle englishBundle, UnzippedResourceBundle bundle) {
        Map<String,Translation<?>> result = new LinkedHashMap<String, Translation<?>>();
        for (Map.Entry<String, String> entry : bundle.staticMessages.entrySet()) {
            String key = entry.getKey();
            result.put(key, new StaticTranslation(englishBundle.staticMessages.get(key), entry.getValue()));
        }
        for (Map.Entry<String, EnumMap<NumericMnemonicResolver.Mnemonic, String>> entry : bundle.numericMessages.entrySet()) {
            String key = entry.getKey();
            result.put(key, new NumericTranslation(englishBundle.numericMessages.get(key).get(NumericMnemonicResolver.Mnemonic.other), entry.getValue()));
        }
        return result;
    }

    private static class UnzippedResourceBundle {
        public String numericMnemonicResolverClass;
        public Map<String,String> staticMessages = new LinkedHashMap<String, String>();
        public Map<String,EnumMap<NumericMnemonicResolver.Mnemonic,String>> numericMessages = new LinkedHashMap<String, EnumMap<NumericMnemonicResolver.Mnemonic, String>>();
    }

    private UnzippedResourceBundle unzip(ResourceBundle bundle) {
        UnzippedResourceBundle result = new UnzippedResourceBundle();
        Enumeration<String> keys = bundle.getKeys();
        Set<String> encounteredKeys = new LinkedHashSet<String>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (encounteredKeys.contains(key)) {
                throw new IllegalStateException("duplicate key: " + key);
            }
            encounteredKeys.add(key);
            String message = bundle.getString(key);
            if (NUMERIC_MNEMONIC_RESOLVER.equals(key)) {
                result.numericMnemonicResolverClass = message;
            } else if (key.contains("#")) {
                String numberlessKey = key.substring(0, key.indexOf("#"));
                if (!result.numericMessages.containsKey(numberlessKey)) {
                    result.numericMessages.put(numberlessKey, new EnumMap<NumericMnemonicResolver.Mnemonic, String>(NumericMnemonicResolver.Mnemonic.class));
                }
                result.numericMessages.get(numberlessKey).put(NumericMnemonicResolver.Mnemonic.valueOf(key.substring(key.indexOf('#') + 1)), message);
            } else {
                result.staticMessages.put(key, message);
            }
        }
        for (Map.Entry<String, EnumMap<NumericMnemonicResolver.Mnemonic, String>> entry : result.numericMessages.entrySet()) {
            if (entry.getValue().size() != NumericMnemonicResolver.Mnemonic.values().length) {
                throw new IllegalStateException(("incomplete mnemonic set: " + entry.getKey()));
            }
        }
        return result;
    }

    private void setNumericMnemonicResolver(String klass) {
        try {
            numericMnemonicResolver = (NumericMnemonicResolver)getClass().getClassLoader().loadClass(klass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getMessage(Class<? extends T> type, String key, String expectedEnglish) {
        Translation<?> translation = translations.get(key);
        if (translation == null) {
            translation = english.get(key);
            if (translation == null) {
                throw new IllegalArgumentException("unrecognized key: " + key);
            }
        }
        if (!translation.getType().equals(type)) {
            throw new IllegalArgumentException(key + " is of type " + translation.getType() + ", not " + type);
        }
        String englishPattern = translation.getEnglishPattern();
        if (!englishPattern.equals(expectedEnglish)) {
            throw new IllegalArgumentException("expected english message '" + expectedEnglish + "' disagrees with the english bundle '" + englishPattern + "'");
        }
        return (T)translation.getInstance();
    }

    protected interface Translation<T> {
        Class<? extends T> getType();
        T getInstance();
        String getEnglishPattern();
    }

    private static class StaticTranslation implements Translation<String> {

        protected final String englishMessage;
        protected final String translatedMessage;

        public StaticTranslation(String englishMessage, String translatedMessage) {
            this.englishMessage = englishMessage;
            this.translatedMessage = translatedMessage;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }

        @Override
        public String getInstance() {
            return translatedMessage;
        }

        @Override
        public String getEnglishPattern() {
            return englishMessage;
        }

    }

    private class NumericTranslation implements Translation<name.ball.joshua.craftinomicon.recipe.i18n.NumericTranslation> {
        private final String englishOtherTranslation;
        private final EnumMap<NumericMnemonicResolver.Mnemonic,String> plurals;

        public NumericTranslation(String englishOtherTranslation, EnumMap<NumericMnemonicResolver.Mnemonic,String> translatedPlurals) {
            this.englishOtherTranslation = englishOtherTranslation;
            this.plurals = translatedPlurals;
        }

        @Override
        public Class<name.ball.joshua.craftinomicon.recipe.i18n.NumericTranslation> getType() {
            return name.ball.joshua.craftinomicon.recipe.i18n.NumericTranslation.class;
        }

        @Override
        public name.ball.joshua.craftinomicon.recipe.i18n.NumericTranslation getInstance() {
            return new name.ball.joshua.craftinomicon.recipe.i18n.NumericTranslation() {
                @Override
                public String getMessage(int numRecipes) {
                    NumericMnemonicResolver.Mnemonic mnemonic = numericMnemonicResolver.resolve(numRecipes);
                    String pattern = plurals.get(mnemonic);
                    String numString = Integer.toString(numRecipes);
                    return pattern.replace("${num-recipes}", numString).replace("${num-usages}", numString);
                }
            };
        }

        @Override
        public String getEnglishPattern() {
            return englishOtherTranslation;
        }
    }

}
