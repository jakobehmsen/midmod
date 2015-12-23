package midmod.pal;

import java.util.List;
import java.util.Stack;

public class ListConsumable implements Consumable {
    private List<Object> list;
    private int index;

    public ListConsumable(List<Object> list) {
        this.list = list;
    }

    @Override
    public Object peek() {
        return list.get(index);
    }

    @Override
    public void consume() {
        index++;
    }

    @Override
    public boolean atEnd() {
        return index >= list.size();
    }

    private Stack<Integer> marks = new Stack<>();

    @Override
    public void mark() {
        marks.push(index);
    }

    @Override
    public void commit() {
        marks.pop();
    }

    @Override
    public void rollback() {
        index = marks.pop();
    }
}
