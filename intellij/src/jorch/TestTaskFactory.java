package jorch;

import javax.swing.*;
import java.util.function.Consumer;

public class TestTaskFactory {
    private Consumer<Runnable> halter;

    public TestTaskFactory(Consumer<Runnable> halter) {
        this.halter = halter;
    }

    public void forkAndMerge(Token token, Scheduler scheduler) {
        TaskFuture<String> s1 = scheduler.call(new TaskSelector("enterValue", new Object[]{"Name"}));
        TaskFuture<String> s2 = scheduler.call(new TaskSelector("enterValue", new Object[]{"Age"}));

        String s1Result = s1.get();
        String s2Result = s2.get();

        token.passTo(new TaskSelector("showResult", new Object[]{"Hello " + s1Result + " (" + s2Result + ")"}));
    }

    public void showResult(Token token, Object result) {
        requestHalt(new Runnable() {
            @Override
            public void run() {
                try {
                    token.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return "=> " + result;
            }
        });
    }

    public void enterValue(Token token, String name) {
        requestHalt(new Runnable() {
            @Override
            public void run() {
                String enterValue = JOptionPane.showInputDialog("Please enter " + name);

                if(enterValue != null) {
                    token.finish(enterValue);
                } else {
                    requestHalt(this);
                }
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
}
