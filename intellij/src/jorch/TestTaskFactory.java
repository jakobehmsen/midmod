package jorch;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class TestTaskFactory {
    private Consumer<Runnable> halter;
    private Function<Callable<?>, ?> haltCaller;

    public TestTaskFactory(Consumer<Runnable> halter, Function<Callable<?>, ?> haltCaller) {
        this.halter = halter;
        this.haltCaller = haltCaller;
    }

    // How to get rid of the Token parameter?
    // How to get rid of the Scheduler parameter?
    // - Could parameters be annotated with @Call("EnterValue", new Object[]{"Name"})?
    // - Could parameters be linked elsewhere to enterValue?
    public void forkAndMerge(Token token, Scheduler scheduler) {
        TaskFuture<String> s1 = scheduler.call(new TaskSelector("enterValue", new Object[]{"Name"}));
        TaskFuture<String> s2 = scheduler.call(new TaskSelector("enterValue", new Object[]{"Age"}));

        String s1Result = s1.get();
        String s2Result = s2.get();

        // Could the method be annotated with a @PassTo("showResult")?
        // Could the method be linked to showResult elsewhere?
        token.passTo(new TaskSelector("showResult", new Object[]{"Hello " + s1Result + " (" + s2Result + ")"}));
    }

    public void showResult(Object result) {
        requestHaltCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }

            @Override
            public String toString() {
                return "=> " + result;
            }
        });
    }

    public Object enterValue(String name) {
        return requestHaltCall(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return JOptionPane.showInputDialog("Please enter " + name);
            }

            @Override
            public String toString() {
                return "Enter " + name;
            }
        });
    }

    private void requestHalt(Runnable activator) {
        halter.accept(activator);
    }

    private <T> T requestHaltCall(Callable<T> activator) {
        return (T) haltCaller.apply(activator);
    }
}
