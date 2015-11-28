package midmod;

public class Tuple3<T extends Model, R extends Model, S extends Model> extends Container {
    public T get1() {
        return (T)getModel(0);
    }

    public R get2() {
        return (R)getModel(1);
    }

    public S get3() {
        return (S)getModel(2);
    }

    public void forAll(TriConsumer<T, R, S> visitor) {
        visitor.accept(get1(), get2(), get3());
    }
}
