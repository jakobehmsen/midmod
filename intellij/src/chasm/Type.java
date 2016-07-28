package chasm;

public class Type extends Entity {
    public Type withAttribute(String name, Object value) {
        return (Type) super.withAttribute(name, value);
    }
}
