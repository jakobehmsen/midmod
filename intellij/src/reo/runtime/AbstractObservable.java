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

    protected void sendInitialize() {
        observers.forEach(x -> x.initialize());
    }

    protected void sendChange(Object value) {
        new ArrayList<>(observers).forEach(x -> x.handle(value));
    }

    protected void sendError(Object error) {
        observers.forEach(x -> x.error(error));
    }

    protected void sendRelease() {
        observers.forEach(x -> x.release());
    }

    protected abstract void sendStateTo(Observer observer);
}
