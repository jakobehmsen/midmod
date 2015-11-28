package midmod;

public class IntModel extends Model {
    private int value;

    public IntModel(int value) {
        this.value = value;
    }

    @Override
    public boolean filter(Model otherModel, Container output) {
        return otherModel instanceof IntModel && this.value == ((IntModel)otherModel).value;
    }
}
