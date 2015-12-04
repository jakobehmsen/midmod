package midmod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListCell extends Cell {
    public static class InsertChange {
        private int index;
        private Object element;

        public InsertChange(int index, Object element) {
            this.index = index;
            this.element = element;
        }

        public int getIndex() {
            return index;
        }

        public Object getElement() {
            return element;
        }
    }

    public static class RemoveChange {
        private int index;

        public RemoveChange(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }


    private ArrayList<Object> elements = new ArrayList<>();

    public void add(Object element) {
        add(elements.size() - 1, element);
    }

    public void add(int index, Object element) {
        elements.add(element);
        sendChange(new InsertChange(index, element));
    }

    public void remove(Object element) {
        int indexOf = elements.indexOf(element);
        elements.remove(indexOf);
        sendChange(new RemoveChange(indexOf));
    }

    @Override
    protected List<Object> getState() {
        return IntStream.range(0, elements.size()).mapToObj(i -> new InsertChange(i, elements.get(i))).collect(Collectors.toList());
    }

    @Override
    public void consumeChange(Object change) {
        if(change instanceof InsertChange) {
            InsertChange insertChange = (InsertChange)change;
            add(insertChange.getIndex(), insertChange.getElement());
        }
    }
}
