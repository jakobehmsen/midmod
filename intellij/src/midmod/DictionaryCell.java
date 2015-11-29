package midmod;

import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

public class DictionaryCell extends Cell implements CellListener {
    @Override
    public void consumeChange(Object change) {
        if(change instanceof PutChange) {
            PutChange putChange = (PutChange)change;
            put(putChange.getKey(), putChange.getValue());
        } else if(change instanceof RemoveChange) {
            RemoveChange remove = (RemoveChange)change;
            remove(remove.getKey());
        }
    }

    public static class PutChange {
        private Object key;
        private Object value;

        public PutChange(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class RemoveChange {
        private Object key;

        public RemoveChange(Object key) {
            this.key = key;
        }

        public Object getKey() {
            return key;
        }
    }

    private Hashtable<Object, Object> map = new Hashtable<>();

    public void put(Object key, Object value) {
        map.put(key, value);
        sendChange(new PutChange(key, value));
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public void remove(Object key) {
        map.remove(key);
        sendChange(new RemoveChange(key));
    }

    @Override
    protected List<Object> getState() {
        return map.entrySet().stream().map(x -> new PutChange(x.getKey(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
