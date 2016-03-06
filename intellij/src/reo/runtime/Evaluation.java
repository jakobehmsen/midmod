package reo.runtime;

import java.util.List;

public class Evaluation {
    private Universe universe;
    private List<RObject> arguments;
    private RObject returnedValue;

    public Evaluation(Universe universe, List<RObject> arguments) {
        this.universe = universe;
        this.arguments = arguments;
    }

    public boolean hasReturned() {
        return returnedValue != null;
    }

    public RObject valueReturned() {
        return returnedValue;
    }

    public Universe getUniverse() {
        return universe;
    }

    public RObject getArgument(int index) {
        return arguments.get(index);
    }

    public void returnValue(RObject value) {
        returnedValue = value;
    }
}
