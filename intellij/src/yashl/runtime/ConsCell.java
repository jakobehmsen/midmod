package yashl.runtime;

import java.util.List;

public class ConsCell {
    private Object car;
    private ConsCell cdr;

    public ConsCell(Object car, ConsCell cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    public Object getCar() {
        return car;
    }

    public ConsCell getCdr() {
        return cdr;
    }

    public static Object cons(List<Object> list) {
        ConsCell value = null;
        for(int i = list.size() - 1; i >= 0; i--)
            value = new ConsCell(list.get(i), value);
        return value;
    }

    @Override
    public String toString() {
        return "(" + toString(this, true) + ")";
    }

    private static String toString(ConsCell cell, boolean atFirst) {
        return cell != null
            ? (!atFirst ? " " : "") + cell.getCar().toString() + toString(cell.getCdr(), false)
            : "";
    }
}
