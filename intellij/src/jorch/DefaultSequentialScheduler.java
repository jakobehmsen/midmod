package jorch;

import java.util.ArrayList;
import java.util.function.Consumer;

public class DefaultSequentialScheduler implements SequentialScheduler {
    private EventHandlerContainer eventHandlerContainer = new EventHandlerContainer();
    private Consumer<SequentialScheduler> nextTask;
    private Object result;

    @Override
    public EventHandlerContainer getEventHandlerContainer() {
        return eventHandlerContainer;
    }

    @Override
    public void finish(Object result) {
        this.result = result;
        nextTask = null;
        finished(result);
        eventHandlerContainer.fireEvent(new DefaultEvent<SequentialSchedulerEventHandler>(SequentialSchedulerEventHandler.class) {
            @Override
            public void beHandledBy(SequentialSchedulerEventHandler eventHandler) {
                eventHandler.finished();
            }
        });
    }

    @Override
    public void scheduleNext(Consumer<SequentialScheduler> nextTask) {
        this.nextTask = nextTask;
        scheduledNext(nextTask);
        eventHandlerContainer.fireEvent(new DefaultEvent<SequentialSchedulerEventHandler>(SequentialSchedulerEventHandler.class) {
            @Override
            public void beHandledBy(SequentialSchedulerEventHandler eventHandler) {
                eventHandler.proceeded();
            }
        });
    }

    public Object proceedToFinish() {
        if(isFinished())
            throw new RuntimeException("Finished");

        while(hasMore())
            proceed();
        Object result = getResult();

        concurrentSchedulers.forEach(cs -> {
            try {
                cs.close();
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

    private ArrayList<ConcurrentScheduler> concurrentSchedulers = new ArrayList<>();

    @Override
    public ConcurrentScheduler split() {
        concurrentSchedulers.add(newConcurrentScheduler());
        return concurrentSchedulers.get(concurrentSchedulers.size() - 1);
    }

    @Override
    public void close() throws Exception {
        wasClosed();
        eventHandlerContainer.fireEvent(new DefaultEvent<SequentialSchedulerEventHandler>(SequentialSchedulerEventHandler.class) {
            @Override
            public void beHandledBy(SequentialSchedulerEventHandler eventHandler) {
                eventHandler.wasClosed();
            }
        });
    }

    protected void scheduledNext(Consumer<SequentialScheduler> nextTask) {

    }

    protected void finished(Object result) {

    }

    protected void wasClosed() {

    }

    protected ConcurrentScheduler newConcurrentScheduler() {
        return new DefaultConcurrentScheduler();
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
