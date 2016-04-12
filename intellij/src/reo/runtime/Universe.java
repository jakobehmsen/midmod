package reo.runtime;

public class Universe {
    private Constant integerPrototypeObservable = new Constant(new Dictionary());
    private Constant nullPrototype = new Constant(new Dictionary());
    private Constant stringPrototypeObservable = new Constant(new Dictionary());

    public Observable getIntegerPrototypeObservable() {
        return integerPrototypeObservable;
    }

    public Dictionary getIntegerPrototype() {
        return (Dictionary)integerPrototypeObservable.getValue();
    }

    public Observable getNull() {
        return nullPrototype;
    }

    public Dictionary getStringPrototype() {
        return (Dictionary)stringPrototypeObservable.getValue();
    }

    public Observable getStringPrototypeObservable() {
        return stringPrototypeObservable;
    }
}
