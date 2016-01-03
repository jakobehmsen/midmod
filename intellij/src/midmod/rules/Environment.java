package midmod.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Environment {
    private int index;
    private ArrayList<Object> captures = new ArrayList<>();

    public Environment getCurrent() {
        return (Environment)captures.get(index);
    }

    public void captureSingle(Object value) {
        //captures.add(index, value);
        //index++;
    }

    public void startCompositeCapture() {
        captures.add(index, new Environment());
    }

    public void endCompositeCapture() {
        // Transform environment into list
        captures.set(index, getCurrent().captures);
        index++;
    }

    public Object get(int index) {
        return captures.get(index);
    }

    public Object getByAddress(int... indices) {
        Object value = get(indices[0]);
        for(int i = 1; i < indices.length; i++)
            value = ((Environment)value).get(indices[i]);
        return value;
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

    public List<Object> toList() {
        return captures.subList(0, index);
    }

    public void captureSingle(int index, Object value) {
        while(captures.size() < index + 1)
            captures.add(null);
        captures.set(index, value);
    }
}
