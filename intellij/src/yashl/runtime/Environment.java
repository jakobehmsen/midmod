package yashl.runtime;

import java.util.Hashtable;
public class Environment {
    private Hashtable<Integer, Object> values = new Hashtable<>();

    public void set(Integer symbolCode, Object value) {
        values.put(symbolCode, value);
    }

    public Object get(int code) {
        return values.get(code);
    }
}
