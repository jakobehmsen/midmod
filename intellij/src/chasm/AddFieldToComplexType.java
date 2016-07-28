package chasm;

public class AddFieldToComplexType implements Change {
    private String name;
    private String fieldName;
    private Field field;

    public AddFieldToComplexType(String name, String fieldName, Field field) {
        this.name = name;
        this.fieldName = fieldName;
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Field getField() {
        return field;
    }

    @Override
    public void perform(Image image) {
        ((ComplexType)image.getType(name)).addField(fieldName, field);
    }
}
