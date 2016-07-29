package chasm;

public class SetAttributeInFieldInComplexType implements Change {
    private String typeName;
    private String fieldName;
    private String attributeName;
    private Object attributeValue;

    public SetAttributeInFieldInComplexType(String typeName, String fieldName, String attributeName, Object attributeValue) {
        this.typeName = typeName;
        this.fieldName = fieldName;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    @Override
    public void perform(Image image) {
        ((ComplexType)image.getType(typeName)).getField(fieldName).withAttribute(attributeName, attributeValue);
    }
}
