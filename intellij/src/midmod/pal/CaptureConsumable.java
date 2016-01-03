package midmod.pal;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CaptureConsumable implements Consumable {
    private List<Object> capturedElements;
    private int capturedElementsCount;
    private Consumable consumable;

    public CaptureConsumable(Consumable consumable) {
        this.capturedElements = new ArrayList<>();
        this.consumable = consumable;
    }

    @Override
    public Object peek() {
        return consumable.peek();
    }

    @Override
    public void consume() {
        propogate(consumable.peek());
        consumable.consume();
    }

    @Override
    public boolean atEnd() {
        return consumable.atEnd();
    }

    private Stack<Integer> markings = new Stack<>();

    @Override
    public void mark() {
        markings.push(capturedElementsCount);
        consumable.mark();
    }

    @Override
    public void commit() {
        markings.pop();
        consumable.commit();
    }

    @Override
    public void rollback() {
        capturedElementsCount = markings.pop();
        consumable.rollback();
    }

    @Override
    public void propogate(Object value) {
        capturedElements.add(value);
        capturedElementsCount++;
    }

    public List<Object> getCapturedElements() {
        return capturedElements.subList(0, capturedElementsCount);
    }
}
