package midmod;

import java.util.function.BiConsumer;

public class Tuple2<T extends Model, R extends Model> extends Container {
    public T get1() {
        return (T)getModel(0);
    }

    public R get2() {
        return (R)getModel(1);
    }

    public <S extends Model> Tuple3<T, R, S> concat(Tuple1<S> tail) {
        Tuple3<T, R, S> tuple = new Tuple3<>();

        tuple.addModel(get1());
        tuple.addModel(get2());
        tuple.addModel(tail.get1());

        return tuple;
    }

    public void forAll(BiConsumer<T, R> visitor) {
        visitor.accept(get1(), get2());
    }
}
