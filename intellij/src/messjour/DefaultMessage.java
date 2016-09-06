package messjour;

public class DefaultMessage implements Message {
    private String name;
    private Object[] arguments;

    public DefaultMessage(String name, Object[] arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int arity() {
        return arguments.length;
    }

    @Override
    public Object getArgument(int ordinal) {
        return arguments[ordinal];
    }
}
