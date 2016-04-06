package reo.runtime;

public interface ReducerConstructor {
    Reducer create(Dictionary self, Observable[] arguments);
}
