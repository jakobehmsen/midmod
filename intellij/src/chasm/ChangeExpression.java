package chasm;

public abstract class ChangeExpression {
    public abstract boolean matches(ChangeExpression expression, Captures captures);

    public Object toValue() {
        throw new UnsupportedOperationException();
    }
}
