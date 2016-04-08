package reo.runtime;

public class Universe {
    private Dictionary integerPrototype2 = new Dictionary();
    private Constant integerPrototype = new Constant(integerPrototype2);
    private Constant nullPrototype = new Constant(new Dictionary());

    public Observable getIntegerPrototype() {
        return integerPrototype;
    }

    public Dictionary getIntegerPrototype2() {
        return integerPrototype2;
    }

    public Observable getNull() {
        return nullPrototype;
    }
}
