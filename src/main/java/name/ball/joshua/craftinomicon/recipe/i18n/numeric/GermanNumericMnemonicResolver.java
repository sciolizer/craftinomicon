package name.ball.joshua.craftinomicon.recipe.i18n.numeric;

import name.ball.joshua.craftinomicon.recipe.i18n.NumericMnemonicResolver;

public class GermanNumericMnemonicResolver implements NumericMnemonicResolver {

    @Override
    public Mnemonic resolve(int n) {
        if (n == 1) return Mnemonic.one;
        if (n == 2) return Mnemonic.two; // German doesn't actually have a special case for 2, but the translator chose to spell it out long hand.
        return Mnemonic.other;
    }

}
