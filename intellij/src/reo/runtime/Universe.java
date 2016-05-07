package reo.runtime;

public class Universe {
    private Constant anyPrototypeObservable;
    private Constant integerPrototypeObservable;
    private Constant nullPrototype;
    private Constant stringPrototypeObservable;
    private Observable componentPrototype;

    public Universe(Dictionary anyPrototype) {
        anyPrototypeObservable = new Constant(anyPrototype);
        integerPrototypeObservable = new Constant(new Dictionary(anyPrototypeObservable));
        nullPrototype = new Constant(new Dictionary(anyPrototypeObservable));
        stringPrototypeObservable = new Constant(new Dictionary(anyPrototypeObservable));
        componentPrototype = new Constant(new Dictionary(anyPrototypeObservable));
    }

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

    public Observable getComponentPrototype() {
        return componentPrototype;
    }
}
