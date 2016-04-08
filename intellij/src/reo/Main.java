package reo;

import reo.runtime.*;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Dictionary d = new Dictionary();

        d.put("asdf", new Constant("sdf"));

        d.put("x", new Constant(2));
        d.put("y", new Constant(4));

        /*d.addObserver(new ReflectiveObserver() {
            void handle(Dictionary.PutSlotChange putSlotChange) {
                System.out.println("Put slot " + putSlotChange.getName() + " to " + putSlotChange.getSlot());
            }
        });

        d.addObserver(new PutSlotAdapter());*/

        //d.get("asdf").addObserver(value -> System.out.println("Put slot asdf to " + value));

        new Reducer(Arrays.asList(d.get("x"), d.get("y")), a -> (int)a[0] + (int)a[1])
            .addObserver(value -> System.out.println("Reduced to: " + value));

        //d.put("asdf", new Constant("fsfsdfsd"));

        /*
        x.i = 7
        x.y(z) => i * z // Is a reducer constructor; when given an observable (z), then a reducer is constructed
        // So, basically, is an observable that produces reducers
        h = x.y(u) // A reducer of i and z is crystalized
        */
        d.put("x", new Constant(7));

        d.put("method", new Constant(new ReducerConstructor() {
            @Override
            public Reducer create(Object self, Dictionary prototype, Observable[] arguments) {
                return new Reducer(Arrays.asList(prototype.get("x"), prototype.get("y"), arguments[0]), a ->
                    (int)a[0] * (int)a[1] * (int)a[2]);
            }
        }));

        d.apply(d, "method", new Observable[]{new Constant(10)})
            .addObserver(new Observer() {
                @Override
                public void handle(Object value) {
                    System.out.println("method reduced to: " + value);
                }

                @Override
                public void release() {
                    System.out.println("method application was released");
                }
            });

        d.put("x", new Constant(8));

        d.put("method", new Constant(new ReducerConstructor() {
            @Override
            public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
                return new Reducer(Arrays.asList(prototype.get("y"), arguments[0]), a ->
                    (int)a[0] * (int)a[1]);
            }
        }));

        d.put("xTimesY", new Reducer(Arrays.asList(d.get("x"), d.get("y")), a -> (int)a[0] * (int)a[1]));
        d.get("xTimesY").addObserver(new Observer() {
            @Override
            public void handle(Object value) {
                System.out.println("xTimesY reduced to: " + value);
            }

            @Override
            public void release() {
                System.out.println("xTimesY was released");
            }
        });

        d.put("x", new Constant(9));

        d.remove("x");

        Universe universe = new Universe();
        universe.getIntegerPrototype().addObserver(new Observer() {
            @Override
            public void handle(Object value) {
                Dictionary integerPrototype = (Dictionary)value;
                integerPrototype.put("+", Observables.constant(new ReducerConstructor() {
                    @Override
                    public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
                        return new Reducer(Arrays.asList(new Constant(self), arguments[0]), a ->
                            (int)a[0] + (int)a[1]);
                    }
                }));
            }
        });
        /*universe.getIntegerPrototype().put("+", Observables.constant(new ReducerConstructor() {
            @Override
            public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
                return new Reducer(Arrays.asList(new Constant(self), arguments[0]), a ->
                    (int)a[0] + (int)a[1]);
            }
        }));*/

        /*
        // obj = {}
        Observables.setSlot(Observables.constant(d), "obj", Observables.constant(new Dictionary()));
        // obj.i = 10
        Observables.setSlot(Observables.getSlot(Observables.constant(d), "obj"), "i", Observables.constant(10));
        // obj.m = 11
        Observables.setSlot(Observables.getSlot(Observables.constant(d), "obj"), "m", Observables.constant(11));
        // j = obj.i
        Observables.setSlot(Observables.constant(d), "j", Observables.getSlot(Observables.getSlot(Observables.constant(d), "obj"), "i"));
        // obj.i = 15
        Observables.setSlot(Observables.getSlot(Observables.constant(d), "obj"), "i", Observables.constant(15));
        // someSum() => obj.i + obj.m
        Observables.setSlot(Observables.getSlot(Observables.constant(d), "obj"), "someSum", Observables.messageSend(
            universe, Observables.getSlot(Observables.getSlot(Observables.constant(d), "obj"), "i"), "+", new Observable[]{Observables.getSlot(Observables.getSlot(Observables.constant(d), "obj"), "m")}));

        Observables.getSlot(Observables.getSlot(Observables.constant(d), "obj"), "someSum").addObserver(new Observer() {
            @Override
            public void handle(Object value) {
                System.out.println("someSum = " + value);
            }
        });
        */

        /*
        - A first language
        */

        Frame frame = new Frame(null, new Instruction[] {
            Instructions.load(0),
            Instructions.newDict(),
            Instructions.storeSlot("obj"),

            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadConstant(10),
            Instructions.storeSlot("i"),

            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadConstant(11),
            Instructions.storeSlot("m"),

            Instructions.load(0),
            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadSlot("i"),
            Instructions.storeSlot("j"),

            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadConstant(15),
            Instructions.storeSlot("i"),

            Instructions.load(0),
            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadSlot("i"),
            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadSlot("m"),
            Instructions.messageSend("+", 1),
            Instructions.storeSlot("someSum"),



            Instructions.load(0),
            Instructions.loadConstant(8),
            Instructions.storeSlot("x"),

            Instructions.load(0),
            Instructions.loadConstant(18),
            Instructions.storeSlot("y"),

            Instructions.load(0),

            Instructions.load(0),
            Instructions.loadSlot("x"),
            Instructions.load(0),
            Instructions.loadSlot("y"),
            Instructions.addi(),

            Instructions.storeSlot("sum"),

            Instructions.load(0),
            Instructions.loadConstant(10),
            Instructions.storeSlot("y"),

            Instructions.load(0),
            Instructions.removeSlot("y"),

            //Instructions.loadConstant("Finished"),
            Instructions.halt()
        });
        Dictionary self = new Dictionary();

        self.addObserver(new ReflectiveObserver() {
            void handle(Dictionary.PutSlotChange putSlotChange) {
                System.out.println("Allocated slot " + putSlotChange.getName());

                putSlotChange.getSlot().addObserver(new Observer() {
                    @Override
                    public void handle(Object value) {
                        System.out.println("Set slot " + putSlotChange.getName() + " to " + value);
                    }

                    @Override
                    public void release() {
                        System.out.println("Deallocated slot " + putSlotChange.getName());
                    }
                });
            }
        });

        frame.push(new Constant(self));
        Evaluation evaluation = new Evaluation(universe, frame);
        evaluation.evaluate();
        Observable result = evaluation.getFrame().pop();
        System.out.println(result);
    }
}
