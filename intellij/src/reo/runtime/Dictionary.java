package reo.runtime;

import java.util.Arrays;
import java.util.Hashtable;

public class Dictionary extends AbstractObservable {
    public static class SlotChange extends Exception {
        private String name;

        public SlotChange(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class PutSlotChange extends SlotChange {
        private Object newValue;

        public PutSlotChange(String name, Object newValue) {
            super(name);
            this.newValue = newValue;
        }

        public Object getNewValue() {
            return newValue;
        }
    }

    public static class RemoveSlotChange extends SlotChange {
        public RemoveSlotChange(String name) {
            super(name);
        }
    }

    private static class Slot extends AbstractObservable implements Observer {
        private Observable observable;
        private Object value;

        @Override
        protected void sendStateTo(Observer observer) {
            if(value != null)
                observer.handle(value);
        }

        @Override
        public void handle(Object value) {
            this.value = value;
            sendChange(value);
        }

        @Override
        public void release() {
            sendRelease();
        }

        public void changeObservable(Observable observable) {
            if(this.observable != null)
                observable.removeObserver(this);
            this.observable = observable;
            observable.addObserver(this);
        }
    }

    private Hashtable<String, Slot> slots = new Hashtable<>();

    public void put(String name, Observable value) {
        Slot slot;
        if(slots.containsKey(name)) {
            slot = slots.get(name);
            slot.changeObservable(value);
        } else {
            slot = new Slot();
            slot.addObserver(new Observer() {
                @Override
                public void handle(Object value) {

                }

                @Override
                public void release() {
                    slots.remove(name);
                }
            });
            slot.changeObservable(value);
            slots.put(name, slot);
        }
    }

    public Observable get(String name) {
        return slots.get(name);
    }

    public void remove(String name) {
        slots.get(name).release(); // Release implicitly remove slot from map
    }

    private class Application extends AbstractObservable implements Observer {
        private Observable[] arguments;
        private Reducer reducer;
        private Object value;

        private Application(Observable[] arguments) {
            this.arguments = arguments;
        }

        @Override
        protected void sendStateTo(Observer observer) {
            if(value != null)
                observer.handle(value);
        }

        @Override
        public void handle(Object value) {
            reducer = ((ReducerConstructor)value).create(Dictionary.this, arguments);
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
    }

    public Observable apply(String name, Observable[] arguments) {
        Application application = new Application(arguments);

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
}
