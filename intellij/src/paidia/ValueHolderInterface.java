package paidia;

import java.awt.*;

public interface ValueHolderInterface extends Value2 {
    void setLocation(Point location);

    Point getLocation();

    interface ValueHolderObserver extends Value2Observer {
        default void updated() { }

        void setValue();
        void setLocation();
    }

    void setValue(Value2 value);
    Value2 getValue();
}
