package chasm;

import java.util.Hashtable;

public class Interaction extends Entity {
    // Local variables
    // Representation

    private Hashtable<String, Type> values = new Hashtable<>();
    private View view;

    public Interaction(View view) {
        this.view = view;
    }

    public void putVariable(String name, Type type) {
        values.put(name, type);
    }

    public Type getVariableType(String name) {
        return values.get(name);
    }
}
