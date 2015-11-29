package midmod;

import java.util.ArrayList;
import java.util.List;

public abstract class Cell implements CellListener {
    public static class CreatedChange {
        private Class<?> type;

        public CreatedChange(Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }
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

        listener.consumeChange(new CreatedChange(getClass()));
        getState().forEach(x -> listener.consumeChange(x));

        return this;
    }

    public void removeListener(CellListener listener) {
        listeners.remove(listener);
    }

    protected void sendChange(Object change) {
        listeners.forEach(x -> x.consumeChange(change));
    }

    protected abstract List<Object> getState();
}
