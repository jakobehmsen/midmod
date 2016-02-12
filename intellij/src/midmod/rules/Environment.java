package midmod.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Environment {
    private int index;
    private ArrayList<Object> captures = new ArrayList<>();

    public Object get(int index) {
        return captures.get(index);
    }

    private Stack<Integer> markings = new Stack<>();

    public void mark() {
        markings.push(index);
    }

    public void commit() {
        markings.pop();
    }

    public void rollback() {
        index = markings.pop();
    }

    public void capture(int index, Object value) {
        while(captures.size() < index + 1)
            captures.add(null);
        /*if(value instanceof ValueConvertible)
            value = ((ValueConvertible)value).toValue();*/
        captures.set(index, value);
        this.index = Math.max(index, this.index);
    }

    public List<Object> getCaptured() {
        return captures.subList(0, index + 1);
    }
}
