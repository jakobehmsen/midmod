package chasm;

public abstract class ChangeExpression {
    public abstract boolean matches(ChangeExpression expression, Captures captures);
}
