package reo.runtime;

public interface ReducerConstructor {
    Observable create(Object self, Dictionary prototype, Observable[] arguments);
}
