package messjour;

import java.util.Hashtable;
import java.util.IdentityHashMap;

public class ObjectPool {
    private IdentityHashMap<Object, Integer> referenceIdMap = new IdentityHashMap<>();
    private Hashtable<Integer, Object> referenceMap = new Hashtable<>();

    public int add(Object object) {
        int id = referenceMap.size();
        referenceIdMap.put(object, id);
        referenceMap.put(id, object);
        return id;
    }

    public Object get(int id) {
        return referenceMap.get(id);
    }

    public int getId(Object object) {
        return referenceIdMap.get(object);
    }
}
