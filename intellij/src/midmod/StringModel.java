package midmod;

public class StringModel extends Model {
    private String value;

    public StringModel(String value) {
        this.value = value;
    }

    @Override
    public boolean filter(Model otherModel, Container output) {
        return otherModel instanceof StringModel && this.value.equals(((StringModel)otherModel).value);
    }

    @Override
    public String toString() {
        return value;
    }
}
