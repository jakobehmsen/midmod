package reo.runtime;

public class Method implements ReducerConstructor {
    private Universe universe;
    private Behavior behavior;

    public Method(Universe universe, Behavior behavior) {
        this.universe = universe;
        this.behavior = behavior;
    }

    @Override
    public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
        Frame returnFrame = new Frame(null, new Instruction[]{Instructions.halt()});
        Evaluation evaluation = new Evaluation(universe, behavior.createFrame(returnFrame, arguments));

        evaluation.evaluate();

        return returnFrame.pop();
    }
}
