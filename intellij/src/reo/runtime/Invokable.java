package reo.runtime;

public interface Invokable {
    Observable invoke(Observable self, Observable[] arguments);
}
