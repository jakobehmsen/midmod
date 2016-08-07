package chasm;

import java.util.*;
import java.util.function.Function;

public class Captures {
    private Hashtable<String, CapturedValue> capturedValueMap = new Hashtable<>();
    private ArrayList<String> declarationOrder = new ArrayList<>();

    public Set<Map.Entry<String, CapturedValue>> entries() {
        return capturedValueMap.entrySet();
    }

    public CapturedValue computeIfAbsent(String id, Function<String, CapturedValue> o) {
        return capturedValueMap.computeIfAbsent(id, k -> {
            declarationOrder.add(k);
            return o.apply(k);
        });
    }

    public CapturedValue get(String id) {
        return capturedValueMap.get(id);
    }

    public Set<String> keySet() {
        return capturedValueMap.keySet();
    }

    public List<String> declarationOrder() {
        return declarationOrder;
    }
}
