package chasm;

public class ThisChangeExpression extends ChangeExpression {
    @Override
    public boolean matches(ChangeExpression expression, Captures captures) {
        return expression instanceof ThisChangeExpression;
    }

    @Override
    public String toString() {
        return "this";
    }
}
