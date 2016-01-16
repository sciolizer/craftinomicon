package name.ball.joshua.craftinomicon.recipe.i18n.numeric;

import name.ball.joshua.craftinomicon.recipe.i18n.NumericMnemonicResolver;

public class RussianMnemonicResolver implements NumericMnemonicResolver {

    @Override
    public Mnemonic resolve(int n) {
        int modTen = n % 10;
        int modHundred = n % 100;
        if (modTen == 1 && modHundred != 11) {
            return Mnemonic.one;
        } else if (modTen >= 2 && modTen <= 4 && (modHundred < 12 || modHundred > 14)) {
            return Mnemonic.few;
        } else if (modTen == 0 || (modTen >= 5 && modTen <= 9) || (modHundred >= 11 && modHundred <= 14)) {
            return Mnemonic.many;
        } else {
            return Mnemonic.other;
        }
    }

}
