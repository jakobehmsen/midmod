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
    private Object[] argumentErrors;
    private Object reduction;
    private Object reductionErrors;

    public Reducer(List<Observable> observables, Function<Object[], Object> function) {
        this.observables = observables;
        arguments = new Object[observables.size()];
        argumentErrors = new Object[observables.size()];
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
                    argumentErrors[i] = null;
                    Object reductionBefore = reduction;
                    update();
                    if(reduction != null) {
                        if(reductionBefore == null)
                            sendInitialize();
                        sendChange(reduction);
                    } else {
                        /*if(reductionBefore != null)
                            sendRelease();*/
                        error(reductionErrors);
                    }
                }

                @Override
                public void error(Object error) {
                    argumentErrors[i] = error;
                }

                @Override
                public void release() {
                    sendRelease();
                }
            });
        });
    }

    private String mapToError(int index) {
        if(argumentErrors[index] != null)
            return argumentErrors[index].toString();
        else if(arguments[index] == null)
            return "Undefined argument at " + index;

        return null;
    }

    private void update() {
        reduction = null;
        reductionErrors = null;

        List<String> errors =
            IntStream.range(0, observables.size()).mapToObj(i -> mapToError(i)).filter(x -> x != null).map(x -> x.toString()).collect(Collectors.toList());
        reductionErrors = errors.toString();

        if(errors.size() > 0) {
            reductionErrors = errors.toString();
        } else if(argumentCount == observables.size()) {
            reduction = function.apply(arguments);
        } else {
            reduction = null;
        }
    }

    @Override
    protected void sendStateTo(Observer observer) {
        if(reduction != null)
            observer.handle(reduction);
        else if(reductionErrors != null)
            observer.error(reductionErrors);
    }

    @Override
    public String toString() {
        return "" + function + "(" + Arrays.asList(arguments).stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + ")";
    }
}
