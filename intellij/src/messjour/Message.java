package messjour;

public interface Message {
    String getName();
    int arity();
    Object getArgument(int ordinal);
    default int getIntArgument(int ordinal) {
        return (int)getArgument(ordinal);
    }
    default String getSelector() {
        return getName() + "/" + arity();
    }
}
