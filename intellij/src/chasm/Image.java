package chasm;

import java.util.Hashtable;
import java.util.Set;

public class Image {
    private Hashtable<String, ComplexType> types = new Hashtable<>();

    public void addType(String name, ComplexType type) {
        types.put(name, type);
    }

    public void removeType(String name) {
        types.remove(name);
    }

    public void renameType(String oldName, String newName) {
        ComplexType t = types.get(oldName);
        types.remove(oldName);
        types.put(newName, t);
    }

    public Type getType(String name) {
        return types.get(name);
    }

    public Set<String> getTypeNames() {
        return types.keySet();
    }
}
