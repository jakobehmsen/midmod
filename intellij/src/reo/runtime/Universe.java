package reo.runtime;

public class Universe {
    private Constant integerPrototypeObservable = new Constant(new DeltaObject());
    private Constant nullPrototype = new Constant(new DeltaObject());
    private Constant stringPrototypeObservable = new Constant(new DeltaObject());
    private Constant dictPrototypeObservable = new Constant(new DeltaObject());

    public Observable getIntegerPrototypeObservable() {
        return integerPrototypeObservable;
    }

    public DeltaObject getIntegerPrototype() {
        return (DeltaObject)integerPrototypeObservable.getValue();
    }

    public Observable getNull() {
        return nullPrototype;
    }

    public DeltaObject getStringPrototype() {
        return (DeltaObject)stringPrototypeObservable.getValue();
    }

    public Observable getStringPrototypeObservable() {
        return stringPrototypeObservable;
    }
}
