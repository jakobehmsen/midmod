package reo.runtime;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.function.Function;

public class Dictionary extends AbstractObservable {
    private Observable prototypeObservable;
    private Dictionary prototype;

    public Dictionary(Observable prototypeObservable) {
        this.prototypeObservable = prototypeObservable;

        if(prototypeObservable != null) {
            prototypeObservable.addObserver(new Observer() {
                @Override
                public void handle(Object value) {
                    prototype = (Dictionary) value;
                    updatePrototypeForEachSlot();
                }
            });
        }
    }

    public Observable getPrototypeObservable() {
        return prototypeObservable;
    }

    private void updatePrototypeForEachSlot() {
        slots.entrySet().forEach(x -> x.getValue().updatePrototype());
    }

    public void update(Object change) {
        change.toString();
    }

    public static class SlotChange {
        private String name;

        public SlotChange(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class PutSlotChange extends SlotChange {
        private Observable slot;

        public PutSlotChange(String name, Observable slot) {
            super(name);
            this.slot = slot;
        }

        public Observable getSlot() {
            return slot;
        }
    }

    public static class RemoveSlotChange extends SlotChange {
        public RemoveSlotChange(String name) {
            super(name);
        }
    }

    private static class Slot extends AbstractObservable implements Observer {
        private final int STATE_UNDEFINED = 0;
        private final int STATE_LOCAL = 1;
        private final int STATE_INHERITED = 2;

        private int state = STATE_UNDEFINED;
        private Binding prototypeBinding;
        private Dictionary dictionary;
        private String name;
        private Observable observable;
        private Object valueInherited;
        private Object value;

        public Slot(Dictionary dictionary, String name, Observable initialObservable) {
            this.dictionary = dictionary;
            this.name = name;

            if(initialObservable != null)
                changeObservable(initialObservable);

            bindToPrototype();
        }

        private void bindToPrototype() {
            if(dictionary.prototype != null) {
                prototypeBinding = dictionary.prototype.get(name).bind(new Observer() {
                    @Override
                    public void initialize() {
                        initialize();
                    }

                    @Override
                    public void handle(Object value) {
                        if (state == STATE_LOCAL)
                            Slot.this.valueInherited = value;
                        else {
                            Slot.this.value = value;
                            state = STATE_INHERITED;
                            sendChange(Slot.this.value);
                        }
                    }

                    @Override
                    public void release() {
                        if (state == STATE_INHERITED) {

                            sendRelease();
                        }
                    }
                });
            }
        }

        public void updatePrototype() {
            prototypeBinding.remove();
            bindToPrototype();
        }

        @Override
        protected void sendStateTo(Observer observer) {
            if(value != null)
                observer.handle(value);
            else
                observer.error("'" + name + "' is undefined.");
        }

        @Override
        public void initialize() {
            sendInitialize();
        }

        @Override
        public void handle(Object value) {
            if(this.value == null && value != null)
                sendInitialize();
            this.value = value;
            state = STATE_LOCAL;
            sendChange(value);
        }

        @Override
        public void error(Object error) {
            sendError("Value error for '" + name + "':" + error);
        }

        @Override
        public void release() {
            if(valueInherited != null) {
                value = valueInherited;
                valueInherited = null;
                state = STATE_INHERITED;
            } else {
                state = STATE_UNDEFINED;
                sendRelease();
            }
        }

        public void changeObservable(Observable observable) {
            if(this.observable != null)
                observable.removeObserver(this);
            //if(prototypeBinding != null)
            //    prototypeBinding.remove();
            this.observable = observable;
            observable.addObserver(this);
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    private Hashtable<String, Slot> slots = new Hashtable<>();

    public void put(String name, Observable value) {
        Slot slot;
        if(slots.containsKey(name)) {
            slot = slots.get(name);
            slot.changeObservable(value);
        } else {
            slot = new Slot(this, name, value);
            /*slot.addObserver(new Observer() {
                @Override
                public void handle(Object value) {

                }

                @Override
                public void release() {
                    slots.remove(name);
                }
            });*/
            slot.changeObservable(value);
            slots.put(name, slot);

            sendChange(new PutSlotChange(name, slot));
        }
    }

    public Observable get(String name) {
        return slots.computeIfAbsent(name, n -> new Slot(this, name, null));

        //return slots.get(name);
    }

    public void remove(String name) {
        slots.get(name).release(); // Release implicitly remove slot from map
    }

    public class Application extends AbstractObservable implements Observer {
        private Object self;
        private String name;
        private Observable[] arguments;
        private Observable reducer;
        private Object value;

        private Application(Object self, String name, Observable[] arguments) {
            this.self = self;
            this.name = name;
            this.arguments = arguments;
        }

        @Override
        protected void sendStateTo(Observer observer) {
            if(value != null)
                observer.handle(value);
        }

        @Override
        public void handle(Object value) {
            reducer = ((ReducerConstructor)value).create(self, Dictionary.this, arguments);
            reducer.bind(new Observer() {
                @Override
                public void handle(Object value) {
                    Application.this.value = value;
                    sendChange(Application.this.value);
                }

                @Override
                public void release() {
                    Application.this.release();
                }
            });
        }

        @Override
        public void release() {
            sendRelease();
        }

        @Override
        public void error(Object error) {
            sendError("Error for application of '" + name + "':" + error);
        }
    }

    public class Application2 extends AbstractObservable implements Observer {
        private Object self;
        private String name;
        private Object[] arguments;
        private Object value;

        private Application2(Object self, String name, Object[] arguments) {
            this.self = self;
            this.name = name;
            this.arguments = arguments;
        }

        @Override
        protected void sendStateTo(Observer observer) {
            if(value != null)
                observer.handle(value);
        }

        @Override
        public void handle(Object value) {
            //Object[] locals = new Object[arguments.length + 1];
            //locals[0] = self;
            //System.arraycopy(arguments, 0, locals, 1, arguments.length);
            //this.value = ((Function<Object[], Object>)value).apply(locals);
            Observable invokable = ((Invokable)value).invoke(new Constant(self),
                Arrays.asList(arguments).stream().map(x -> new Constant(x)).toArray(s -> new Observable[s]));

            invokable.bind(new Observer() {
                @Override
                public void handle(Object value) {
                    Application2.this.value = value;
                    sendChange(Application2.this.value);
                }

                @Override
                public void release() {
                    Application2.this.release();
                }
            });

            //sendChange(value);

            /*reducer = ((ReducerConstructor)value).create(self, Dictionary.this, arguments);
            reducer.bind(new Observer() {
                @Override
                public void handle(Object value) {
                    Application.this.value = value;
                    sendChange(Application.this.value);
                }

                @Override
                public void release() {
                    Application.this.release();
                }
            });*/
        }

        @Override
        public void error(Object error) {
            sendError("Error for application of '" + name + "':" + error);
        }

        @Override
        public void release() {
            sendRelease();
        }
    }



    public Observable apply(Object self, String name, Observable[] arguments) {
        Application application = new Application(self, name, arguments);

        get(name).addObserver(application);

        return application;

        /*
        // The values of the slot must be reducer constructors.
        // A reducer constructor:
        // - must be able to construct reducers when given arguments and a particular self/dictionary
        return new AbstractObservable() {
            @Override
            protected void sendStateTo(Observer observer) {

            }
        };
        */
    }

    public Observable apply2(Object self, String name, Object[] arguments) {
        // Both self and arguments are resolved, but slot value may change
        // Each time slot value changes, the application should emit a new value
        Application2 application = new Application2(self, name, arguments);

        get(name).addObserver(application);

        return application;

        /*
        // The values of the slot must be reducer constructors.
        // A reducer constructor:
        // - must be able to construct reducers when given arguments and a particular self/dictionary
        return new AbstractObservable() {
            @Override
            protected void sendStateTo(Observer observer) {

            }
        };
        */
    }

    @Override
    protected void sendStateTo(Observer observer) {
        slots.entrySet().forEach(x -> sendChange(new PutSlotChange(x.getKey(), x.getValue())));
    }

    @Override
    public String toString() {
        return "#{...}";
    }

    public Observable slots() {
        return new AbstractObservable() {
            Binding observer = bind(new Observer() {
                @Override
                public void handle(Object value) {
                    sendChange(slots.entrySet());
                }

                @Override
                public void release() {

                }
            });

            @Override
            protected void sendStateTo(Observer observer) {
                observer.handle(slots.entrySet());
            }
        };
    }
}
