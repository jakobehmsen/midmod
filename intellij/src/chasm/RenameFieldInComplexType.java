package chasm;

public class RenameFieldInComplexType implements Change {
    private String typeName;
    private String oldFieldName;
    private String newFieldName;

    public RenameFieldInComplexType(String typeName, String oldFieldName, String newFieldName) {
        this.typeName = typeName;
        this.oldFieldName = oldFieldName;
        this.newFieldName = newFieldName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getOldFieldName() {
        return oldFieldName;
    }

    public String getNewFieldName() {
        return newFieldName;
    }

    @Override
    public void perform(Image image) {
        ((ComplexType)image.getType(typeName)).renameField(oldFieldName, newFieldName);
    }
}
