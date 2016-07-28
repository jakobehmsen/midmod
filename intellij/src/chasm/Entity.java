package chasm;

import java.util.Hashtable;

public class Entity {
    private Hashtable<String, Object> attributes = new Hashtable<>();

    public Entity withAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }
}
