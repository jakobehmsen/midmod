package reo.runtime;

import java.util.List;
import java.util.function.Function;
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
                public void handle(Object value) {
                    if(arguments[i] == null && value != null)
                        argumentCount++;
                    if(arguments[i] != null && value == null)
                        argumentCount--;
                    arguments[i] = value;
                    update();
                    if(reduction != null)
                        sendChange(reduction);
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
}
