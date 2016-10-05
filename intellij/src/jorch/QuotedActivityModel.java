package jorch;

public class QuotedActivityModel implements ActivityModel {
    private Step step;

    public QuotedActivityModel(Step step) {
        this.step = step;
    }

    @Override
    public Step toStep() {
        return step;
    }

    @Override
    public String toString() {
        return step.toString();
    }
}
