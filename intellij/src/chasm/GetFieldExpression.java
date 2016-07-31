package chasm;

public class GetFieldExpression extends Expression {
    private Expression expression;
    private String fieldName;

    public GetFieldExpression(Expression expression, String fieldName) {
        this.expression = expression;
        this.fieldName = fieldName;
    }

    @Override
    public Type getResultType(Interaction interaction) {
        return ((ComplexType)expression.getResultType(interaction)).getField(fieldName).getType();
    }
}
