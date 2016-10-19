package jorch;

public class DependencyInjectorContext {
    private static DependencyInjector instance;

    public static void setInstance(DependencyInjector instance) {
        DependencyInjectorContext.instance = instance;
    }

    public static DependencyInjector getInstance() {
        return instance;
    }
}
