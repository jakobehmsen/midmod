package paidia;

import java.util.ArrayList;

public abstract class AbstractValue2 implements Value2 {
    private ArrayList<Value2Observer> observers = new ArrayList<>();

    @Override
    public void addObserver(Value2Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Value2Observer observer) {
        observers.remove(observer);
    }

    protected void sendUpdated() {
        new ArrayList<>(observers).forEach(x -> x.updated());
    }
}
