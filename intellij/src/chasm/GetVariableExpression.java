package chasm;

public class GetVariableExpression extends Expression {
    private String name;

    public GetVariableExpression(String name) {
        this.name = name;
    }

    @Override
    public Type getResultType(Interaction interaction) {
        return interaction.getVariableType(name);
    }
}
