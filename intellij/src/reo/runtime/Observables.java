package reo.runtime;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Observables {
    public static void setSlot(Observable target, String name, Observable valueObs) {
        target.addObserver(new Observer() {
            private Binding lastDictBinding;

            @Override
            public void handle(Object value) {
                if(lastDictBinding != null)
                    lastDictBinding.remove();
                ((DeltaObject)value).put(name, valueObs);
                lastDictBinding = () -> ((DeltaObject)value).remove(name);
            }

            @Override
            public String toString() {
                return target + "." + name + " = " + valueObs;
            }
        });
    }

    public static void removeSlot(Observable target, String name) {
        target.addObserver(new Observer() {
            private Binding lastDictBinding;

            @Override
            public void handle(Object value) {
                if(lastDictBinding != null)
                    lastDictBinding.remove();
                ((DeltaObject)value).remove(name);
                lastDictBinding = () -> ((DeltaObject)value).remove(name);
            }

            @Override
            public String toString() {
                return "delete " + target + "." + name;
            }
        });
    }

    public static Observable getSlot(Observable target, String name) {
        return new AbstractObservable() {
            private Binding binding;
            private Object value;

            {
                target.addObserver(new Observer() {
                    @Override
                    public void handle(Object deltaObjectValue) {
                        if(binding != null)
                            binding.remove();

                        DeltaObject deltaObject = (DeltaObject)deltaObjectValue;
                        binding = deltaObject.get(name).bind(new Observer() {
                            @Override
                            public void handle(Object newValue) {
                                value = newValue;
                                sendChange(value);
                            }
                        });
                    }
                });
            }

            @Override
            protected void sendStateTo(Observer observer) {
                if(value != null)
                    observer.handle(value);
            }

            @Override
            public String toString() {
                return target + "." + name;
            }
        };
    }

    public static Observable messageSend(Universe universe, Observable receiverObs, String selector, Observable[] arguments) {
        return new AbstractObservable() {
            private Binding binding;
            private DeltaObject receiverPrototype;
            private Object response;

            {
                receiverObs.addObserver(new Observer() {
                    @Override
                    public void handle(Object value) {
                        if(binding != null)
                            binding.remove();

                        receiverPrototype = getPrototype(universe, value);
                        Observable application = receiverPrototype.apply(value, selector, arguments);

                        binding = new Binding() {
                            Binding binding = application.bind(new Observer() {
                                @Override
                                public void handle(Object value) {
                                    response = value;
                                    sendChange(response);
                                }
                            });

                            @Override
                            public void remove() {
                                receiverPrototype.get(selector).removeObserver((Observer)application);
                                binding.remove();
                            }
                        };
                    }
                });
            }

            @Override
            protected void sendStateTo(Observer observer) {
                if(response != null)
                    observer.handle(response);
            }

            private DeltaObject getPrototype(Universe universe, Object target) {
                if(target instanceof DeltaObject)
                    return (DeltaObject)target;
                if(target instanceof Integer)
                    return universe.getIntegerPrototype();
                if(target instanceof String)
                    return universe.getStringPrototype();

                throw new RuntimeException("Could not resolve prototype for: " + target);
            }

            @Override
            public String toString() {
                return receiverObs + "." + selector + "(" +
                    Arrays.asList(arguments).stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + ")";
            }
        };
    }

    public static Observable constant(Object value) {
        return new AbstractObservable() {
            @Override
            protected void sendStateTo(Observer observer) {
                observer.handle(value);
            }

            @Override
            public String toString() {
                return value.toString();
            }
        };
    }
}
