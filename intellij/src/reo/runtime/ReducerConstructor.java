package reo.runtime;

public interface ReducerConstructor {
    Observable create(Dictionary self, Observable[] arguments);
}
