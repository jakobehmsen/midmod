package chasm;

public abstract class IdChangeExpression {
    public abstract boolean matches(IdChangeExpression idExpression, Captures captures);
}
