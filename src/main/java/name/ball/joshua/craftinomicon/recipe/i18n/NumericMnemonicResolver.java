package name.ball.joshua.craftinomicon.recipe.i18n;

public interface NumericMnemonicResolver {

    Mnemonic resolve(int n);

    public enum Mnemonic {
//        ZERO,
        one,
        two,
        few,
        many,
        other
    }
}
