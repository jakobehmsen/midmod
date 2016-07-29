package chasm;

public class Field extends Entity {
    private Type type;

    public Field(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Field withAttribute(String name, Object value) {
        return (Field) super.withAttribute(name, value);
    }
}
