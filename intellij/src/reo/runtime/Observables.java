package reo.runtime;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Observables {
    public static void setSlot(Observable target, String name, Observable valueObs) {
        target.addObserver(new Observer() {
            private Binding lastDictBinding;

            @Override
            public void handle(Object value) {
                if(lastDictBinding != null)
                    lastDictBinding.remove();
                ((Dictionary)value).put(name, valueObs);
                lastDictBinding = () -> ((Dictionary)value).remove(name);
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
                ((Dictionary)value).remove(name);
                lastDictBinding = () -> ((Dictionary)value).remove(name);
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
            private Object theError;

            {
                target.addObserver(new Observer() {
                    @Override
                    public void handle(Object deltaObjectValue) {
                        if(binding != null)
                            binding.remove();

                        Dictionary dictionary = (Dictionary)deltaObjectValue;
                        binding = dictionary.get(name).bind(new Observer() {
                            @Override
                            public void handle(Object newValue) {
                                value = newValue;
                                sendChange(value);
                            }

                            @Override
                            public void error(Object error) {
                                sendError(error);
                                theError = error;
                            }
                        });
                    }

                    @Override
                    public void error(Object error) {
                        sendError("Target error: " + error);
                    }
                });
            }

            @Override
            protected void sendStateTo(Observer observer) {
                if(value != null)
                    observer.handle(value);
                else if(theError != null)
                    observer.error(theError);
            }

            @Override
            public String toString() {
                return target + "." + name;
            }

            @Override
            public Getter toGetter() {
                return new Getter() {
                    Dictionary dictionary;
                    Binding binding;

                    @Override
                    public void toView(ViewAdapter viewAdapter) {
                        //JTextField view = new JTextField();

                        /*view.getDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                dictionary.put(name, new Constant(view.getText()));
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                dictionary.put(name, new Constant(view.getText()));
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {

                            }
                        });*/

                        binding = target.bind(new Observer() {
                            @Override
                            public void handle(Object deltaObjectValue) {
                                if(binding != null)
                                    binding.remove();

                                dictionary = (Dictionary)deltaObjectValue;

                                dictionary.get(name).bind(new Observer() {
                                    @Override
                                    public void handle(Object newValue) {
                                        dictionary.get(name).removeObserver(this);
                                        //view.setText(newValue.toString());
                                        viewAdapter.initialize(newValue);
                                    }

                                    @Override
                                    public void error(Object error) {

                                    }
                                });
                            }

                            @Override
                            public void error(Object error) {
                                sendError("Target error: " + error);
                            }
                        });

                        viewAdapter.addObserver(new Observer() {
                            @Override
                            public void handle(Object value) {
                                dictionary.put(name, new Constant(value));
                            }
                        });

                        //return view;
                    }

                    @Override
                    public void remove() {
                        binding.remove();
                    }
                };
            }
        };
    }

    public static Observable messageSend(Universe universe, Observable receiverObs, String selector, Observable[] arguments) {
        return new AbstractObservable() {
            private Binding binding;
            private Dictionary receiverPrototype;
            private ReducerConstructor reducerConstructor;
            private Object response;
            //private Object slotValue;
            private Object receiver;
            private Object receiverError;
            private Object[] argumentValues = new Object[arguments.length];
            private Object[] argumentErrors = new Object[arguments.length];

            {
                // Add observers to arguments here and handle these arguments here in the message send (incl. errors) as
                // currently done in Reducer.

                IntStream.range(0, arguments.length).forEach(i -> {
                    arguments[i].addObserver(new Observer() {
                        @Override
                        public void handle(Object value) {
                            argumentValues[i] = value;
                            argumentErrors[i] = null;

                            update();
                            send();
                        }

                        @Override
                        public void error(Object error) {
                            argumentValues[i] = null;
                            argumentErrors[i] = error;

                            update();
                            send();
                        }
                    });
                });

                receiverObs.addObserver(new Observer() {
                    @Override
                    public void handle(Object value) {
                        if(binding != null)
                            binding.remove();

                        receiver = value;
                        receiverError = null;

                        update();
                        send();

                        /*receiverPrototype = getPrototype(universe, value);

                        Observable application = receiverPrototype.apply(value, selector, arguments);

                        binding = new Binding() {
                            Binding binding = application.bind(new Observer() {
                                @Override
                                public void handle(Object value) {
                                    response = value;
                                    update();
                                    send();
                                }

                                @Override
                                public void error(Object error) {
                                    update();
                                    send();
                                }
                            });

                            @Override
                            public void remove() {
                                receiverPrototype.get(selector).removeObserver((Observer)application);
                                binding.remove();
                            }
                        };*/
                    }

                    @Override
                    public void error(Object error) {
                        receiver = null;
                        receiverError = error;

                        update();
                        send();
                    }
                });
            }

            private String mapToError(int index) {
                if(argumentErrors[index] != null)
                    return "Argument error at " + index + ": " + argumentErrors[index].toString();
                else if(arguments[index] == null)
                    return "Undefined argument at " + index;

                return null;
            }

            private ArrayList<String> allErrors;

            private void update() {
                // If receiver is resolved and all arguments are resolved, then the message can be sent
                allErrors = new ArrayList<>();
                if(receiverError != null)
                    allErrors.add("Receiver error: " + receiverError);
                else if(receiver == null)
                    allErrors.add("Receiver is undefined.");
                List<String> errors =
                    IntStream.range(0, arguments.length).mapToObj(i -> mapToError(i)).filter(x -> x != null).map(x -> x.toString()).collect(Collectors.toList());
                allErrors.addAll(errors);
            }

            private void send() {
                if(allErrors.size() == 0) {
                    receiverPrototype = getPrototype(universe, receiver);

                    Observable application = receiverPrototype.apply2(receiver, selector, argumentValues);

                    binding = new Binding() {
                        Binding binding = application.bind(new Observer() {
                            @Override
                            public void handle(Object value) {
                                response = value;
                                sendChange(value);
                            }

                            @Override
                            public void error(Object error) {
                                sendError(error);
                            }
                        });

                        @Override
                        public void remove() {
                            receiverPrototype.get(selector).removeObserver((Observer)application);
                            binding.remove();
                        }
                    };
                } else {
                    sendError(allErrors);
                }
            }

            @Override
            protected void sendStateTo(Observer observer) {
                if(allErrors.size() == 0) {
                    if (response != null)
                        observer.handle(response);
                } else {
                    observer.error(allErrors);
                }
            }

            private Dictionary getPrototype(Universe universe, Object target) {
                if(target instanceof Dictionary)
                    return (Dictionary)target;
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
