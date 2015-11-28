package midmod;

public class Tuple1<T extends Model> extends Container {
    public T get1() {
        return (T)getModel(0);
    }

    public <R extends Model> Tuple2<T, R> concat(Tuple1<R> tail) {
        Tuple2<T, R> tuple = new Tuple2<>();

        tuple.addModel(get1());
        tuple.addModel(tail.get1());

        return tuple;
    }
}
