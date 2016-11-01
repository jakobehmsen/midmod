package jorch;

import java.io.*;

public class DefaultScheduler implements Scheduler {
    private String filePath;
    private State state;

    private interface State extends Serializable {
        void proceed(DefaultScheduler scheduler);
        void resume(DefaultScheduler scheduler);
    }

    public DefaultScheduler(String filePath, Task start) {
        File file = new File(filePath);
        this.filePath = filePath;

        if(file.exists()) {
            try(FileInputStream fileInputStream = new FileInputStream(filePath)) {
                try(ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                    state = (State) objectInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            perform(start);
        }
    }

    private static class PerformState implements State {
        private Task task;

        private PerformState(Task task) {
            this.task = task;
        }

        @Override
        public void proceed(DefaultScheduler scheduler) {
            scheduler.scheduleTask(task);
        }

        @Override
        public void resume(DefaultScheduler scheduler) {

        }
    }

    @Override
    public void perform(Task task) {
        setState(new PerformState(task));
    }

    private static class HaltState implements State {
        private boolean shouldResume;
        private Task task;

        private HaltState(Task task) {
            this.task = task;
        }

        @Override
        public void proceed(DefaultScheduler scheduler) {
            if(shouldResume)
                scheduler.scheduleTask(task);
            else
                scheduler.halted();
        }

        @Override
        public void resume(DefaultScheduler scheduler) {
            shouldResume = true;
        }
    }


    @Override
    public void halt(Task task) {
        setState(new HaltState(task));
    }

    @Override
    public void resume() {
        state.resume(this);
    }

    private void setState(State state) {
        this.state = state;

        try(FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(state);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void scheduleTask(Task task) {
        task.perform(this);
    }

    protected void halted() {

    }

    public void proceed() {
        state.proceed(this);
    }

    @Override
    public void finish() {

    }
}
