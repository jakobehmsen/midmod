package midmod;

import java.util.ArrayList;
import java.util.List;

public abstract class Cell extends CellListener {
    public static class CreatedChange {
        private Class<?> type;

        public CreatedChange(Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }
    }

    public static class NullChange {

    }

    public static class ValueChange {
        private Object value;

        public ValueChange(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    private ArrayList<CellListener> listeners = new ArrayList<>();

    public Cell addListener(CellListener listener) {
        listeners.add(listener);

        //listener.consumeChange(new CreatedChange(getClass()));
        getState().forEach(x -> listener.consumeChange(x));

        return this;
    }

    public <T extends CellListener> T withListener(T listener) {
        listeners.add(listener);
        listener.addSubject(this);

        listener.consumeChange(new CreatedChange(getClass()));
        getState().forEach(x -> listener.consumeChange(x));

        return listener;
    }

    public void removeListener(CellListener listener) {
        listeners.remove(listener);
        listener.removeSubject(this);
    }

    protected List<CellListener> getListeners() {
        return new ArrayList<>(listeners);
    }

    protected void sendChange(Object change) {
        listeners.forEach(x -> x.consumeChange(change));
    }

    @Override
    protected void cleanup() {
        //new ArrayList<>(listeners).forEach(x -> removeListener(x));
    }

    protected abstract List<Object> getState();
}
