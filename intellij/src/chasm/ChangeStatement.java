package chasm;

public abstract class ChangeStatement {
    public abstract boolean matches(ChangeStatement statement, Captures captures);
}
