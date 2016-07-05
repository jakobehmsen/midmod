package paidia;

public interface ValueHolderInterface extends Value2 {
    interface ValueHolderObserver extends Value2Observer {
        default void updated() { }

        void setValue();
    }

    void setValue(Value2 value);
    Value2 getValue();
}
