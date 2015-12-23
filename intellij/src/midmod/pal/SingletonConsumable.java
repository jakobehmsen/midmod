package midmod.pal;

public class SingletonConsumable implements Consumable {
    private Object obj;
    private boolean isConsumed;

    public SingletonConsumable(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object peek() {
        if(isConsumed)
            throw new RuntimeException("Already consumed.");
        return obj;
    }

    @Override
    public void consume() {
        isConsumed = true;
    }

    @Override
    public boolean atEnd() {
        return isConsumed;
    }

    @Override
    public void mark() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }
}
