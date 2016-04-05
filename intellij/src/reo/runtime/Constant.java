package reo.runtime;

public class Constant extends AbstractObservable {
    private Object value;

    public Constant(Object value) {
        this.value = value;
    }

    @Override
    protected void sendStateTo(Observer observer) {
        observer.handle(value);
    }
}
