package midmod.pal;

import java.util.function.Consumer;

public class ObservedConsumable implements Consumable {
    private Consumable consumable;
    private Consumer<Object> observer;

    public ObservedConsumable(Consumable consumable, Consumer<Object> observer) {
        this.consumable = consumable;
        this.observer = observer;
    }

    @Override
    public Object peek() {
        return consumable.peek();
    }

    @Override
    public void consume() {
        propogate(peek());
        consumable.consume();
    }

    @Override
    public boolean atEnd() {
        return consumable.atEnd();
    }

    @Override
    public void mark() {
        consumable.mark();
    }

    @Override
    public void commit() {
        consumable.commit();
    }

    @Override
    public void rollback() {
        consumable.rollback();
    }

    @Override
    public void propogate(Object value) {
        observer.accept(value);
    }
}
