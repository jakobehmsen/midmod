package reo.runtime;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Reducer extends AbstractObservable {
    private List<Observable> observables;
    private Function<Object[], Object> function;
    private Object[] arguments;
    private int argumentCount;
    private Object reduction;

    public Reducer(List<Observable> observables, Function<Object[], Object> function) {
        this.observables = observables;
        arguments = new Object[observables.size()];
        this.function = function;

        IntStream.range(0, observables.size()).forEach(i -> {
            observables.get(i).bind(new Observer() {
                @Override
                public void initialize() {

                }

                @Override
                public void handle(Object value) {
                    if(arguments[i] == null && value != null)
                        argumentCount++;
                    if(arguments[i] != null && value == null)
                        argumentCount--;
                    arguments[i] = value;
                    Object reductionBefore = reduction;
                    update();
                    if(reduction != null) {
                        if(reductionBefore == null)
                            sendInitialize();
                        sendChange(reduction);
                    } else {
                        if(reductionBefore != null)
                            sendRelease();
                    }
                }

                @Override
                public void release() {
                    sendRelease();
                }
            });
        });
    }

    private void update() {
        if(argumentCount == observables.size()) {
            reduction = function.apply(arguments);
        } else {
            reduction = null;
        }
    }

    @Override
    protected void sendStateTo(Observer observer) {
        if(reduction != null)
            observer.handle(reduction);
    }

    @Override
    public String toString() {
        return "" + function + "(" + Arrays.asList(arguments).stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + ")";
    }
}
