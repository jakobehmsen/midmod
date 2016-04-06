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
                System.out.println("Put slot " + putSlotChange.getName() + " to " + putSlotChange.getNewValue());
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
            public Reducer create(Dictionary self, Observable[] arguments) {
                return new Reducer(Arrays.asList(self.get("x"), self.get("y"), arguments[0]), a ->
                    (int)a[0] * (int)a[1] * (int)a[2]);
            }
        }));

        d.apply("method", new Observable[]{new Constant(10)})
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
            public Reducer create(Dictionary self, Observable[] arguments) {
                return new Reducer(Arrays.asList(self.get("y"), arguments[0]), a ->
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
    }
}
