package midmod.rules;

import java.util.ArrayList;
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
        captures.set(index, value);
    }
}