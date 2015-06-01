package name.ball.joshua.craftinomicon.recipe.i18n.numeric;

import name.ball.joshua.craftinomicon.recipe.i18n.NumericMnemonicResolver;

public class ChineseNumericMnemonicResolver implements NumericMnemonicResolver {

    @Override
    public Mnemonic resolve(int n) {
        return Mnemonic.other;
    }

}
