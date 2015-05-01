package name.ball.joshua.craftinomicon.recipe;

public class AbstractTest {
    protected void assertEqual(Object o1, Object o2) {
        if (!equals(o1, o2)) {
            throw new AssertionError("unequal: " + o1 + " != " + o2);
        }
    }

    protected boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
