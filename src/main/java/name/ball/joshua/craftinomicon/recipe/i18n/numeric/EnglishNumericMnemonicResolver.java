package name.ball.joshua.craftinomicon.recipe.i18n.numeric;

import name.ball.joshua.craftinomicon.recipe.i18n.NumericMnemonicResolver;

public class EnglishNumericMnemonicResolver implements NumericMnemonicResolver {

    @Override
    public Mnemonic resolve(int n) {
        return n == 1 ? Mnemonic.one : Mnemonic.other;
    }

}
