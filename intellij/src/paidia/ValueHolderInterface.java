package paidia;

import java.awt.*;
import java.util.Hashtable;
import java.util.Map;

public interface ValueHolderInterface extends Value2 {
    void setLocation(Point location);

    Point getLocation();

    class HeldValueChange extends Change {
        public HeldValueChange(Value2 source) {
            super(source);
        }
    }

    class HeldLocationChange extends Change {
        public HeldLocationChange(Value2 source) {
            super(source);
        }
    }

    /*interface ValueHolderObserver extends Value2Observer {
        default void updated(Change change) { }

        void setValue();
        void setLocation();
    }*/

    void setValue(Value2 value);
    Value2 getValue();

    @Override
    default Value2 getHeldValueOrSelf() {
        return getValue();
    }



    @Override
    default Value2 derive() {
        return new ProjectionValue(this, v -> ((ValueHolderInterface)v).getValue().derive());
    }

    @Override
    default Value2 reduce() {
        return new ProjectionValue(this, v ->
            ((ValueHolderInterface)v).getValue().reduce(new Hashtable<>()));
    }
}