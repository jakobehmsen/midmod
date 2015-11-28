package midmod;

public interface TriConsumer<T, R, S> {
    void accept(T t, R r, S s);
}
