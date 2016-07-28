package chasm;

public class AddComplexType implements Change {
    private String name;

    public AddComplexType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void perform(Image image) {
        image.addType(name, new ComplexType());
    }
}
