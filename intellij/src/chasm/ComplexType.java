package chasm;

import java.util.Hashtable;
import java.util.Set;

public class ComplexType extends Type {
    private Hashtable<String, Field> fields = new Hashtable<>();

    public void addField(String name, Field field) {
        fields.put(name, field);
    }

    public void removeField(String name) {
        fields.remove(name);
    }

    public void renameField(String oldName, String newName) {
        Field f = fields.get(oldName);
        fields.remove(oldName);
        fields.put(newName, f);
    }

    public Set<String> getFieldNames() {
        return fields.keySet();
    }

    public Field getField(String fieldName) {
        return fields.get(fieldName);
    }
}
