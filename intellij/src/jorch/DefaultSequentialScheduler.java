package jorch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DefaultSequentialScheduler implements SequentialScheduler {
    private EventHandlerContainer eventHandlerContainer = new EventHandlerContainer();
    private Consumer<SequentialScheduler> nextTask;
    private Object result;
    private SequentialScheduler parent;

    public DefaultSequentialScheduler(SequentialScheduler parent) {
        this.parent = parent;
    }

    @Override
    public SequentialScheduler getParent() {
        return parent;
    }

    @Override
    public EventHandlerContainer getEventHandlerContainer() {
        return eventHandlerContainer;
    }

    @Override
    public void finish(Object result) {
        this.result = result;
        nextTask = null;
        finished(result);
        eventHandlerContainer.fireEvent(SequentialSchedulerEventHandler.class, eh -> eh.finished());
    }

    @Override
    public void scheduleNext(Consumer<SequentialScheduler> nextTask) {
        this.nextTask = nextTask;
        scheduledNext(nextTask);
        eventHandlerContainer.fireEvent(SequentialSchedulerEventHandler.class, eh -> eh.proceeded());
    }

    @Override
    public SequentialScheduler newSequentialScheduler(Consumer<SequentialScheduler> initialTask) {
        return new DefaultSequentialScheduler(parent);
    }

    @Override
    public List<SequentialScheduler> getSequentialSchedulers() {
        return sequentialSchedulers;
    }

    public void addSequentialScheduler(SequentialScheduler sequentialScheduler) {
        sequentialSchedulers.add(sequentialScheduler);
        getEventHandlerContainer().fireEvent(SequentialSchedulerContainerEventHandler.class, eh -> eh.addedSequentialScheduler(sequentialScheduler));
    }

    private ArrayList<SequentialScheduler> sequentialSchedulers = new ArrayList<>();

    public Object proceedToFinish() {
        if(isFinished())
            throw new RuntimeException("Finished");

        while(hasMore())
            proceed();
        Object result = getResult();

        sequentialSchedulers.forEach(ss -> {
            try {
                ss.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        finished(result);

        return result;
    }

    private boolean isFinished() {
        return nextTask == null;
    }

    public void proceed() {
        nextTask.accept(this);
    }

    protected void setNextTask(Consumer<SequentialScheduler> nextTask) {
        this.nextTask = nextTask;
    }

    public boolean hasMore() {
        return nextTask != null;
    }

    public Object getResult() {
        return result;
    }

    //private ArrayList<ConcurrentScheduler> concurrentSchedulers = new ArrayList<>();

    @Override
    public void close() throws Exception {
        sequentialSchedulers.forEach(ss -> {
            try {
                ss.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        wasClosed();
        eventHandlerContainer.fireEvent(SequentialSchedulerEventHandler.class, eh -> eh.wasClosed());
    }

    protected void scheduledNext(Consumer<SequentialScheduler> nextTask) {

    }

    protected void finished(Object result) {

    }

    protected void wasClosed() {

    }

    protected void setFinished(Object result) {
        nextTask = null;
        this.result = result;
    }

    @Override
    public String toString() {
        if(hasMore())
            return "@" + nextTask.toString();
        return "=> " + result;
    }
}
