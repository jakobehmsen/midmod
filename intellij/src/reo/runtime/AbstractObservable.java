package reo.runtime;

import java.util.ArrayList;

public abstract class AbstractObservable implements Observable {
    private ArrayList<Observer> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
        sendStateTo(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    protected void sendChange(Object value) {
        observers.forEach(x -> x.handle(value));
    }

    protected abstract void sendStateTo(Observer observer);
}
