package midmod;

public class CaptureModel extends Model {
    @Override
    public boolean filter(Model otherModel, Container output) {
        output.addModel(otherModel);
        return true;
    }
}
