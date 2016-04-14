package reo.runtime;

public interface ReducerConstructor {
    Observable create(Object self, DeltaObject prototype, Observable[] arguments);
}
