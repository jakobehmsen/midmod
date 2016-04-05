package reo.runtime;

public interface Observable {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    default Binding bind(Observer observer) {
        addObserver(observer);
        return () -> removeObserver(observer);
    }
}
