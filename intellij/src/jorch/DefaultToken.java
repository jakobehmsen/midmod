package jorch;

import java.util.ArrayList;

public class DefaultToken implements Token {
    private EventChannel eventChannel = new EventChannel();
    private TaskSelector nextTask;
    private Object result;
    private Token parent;
    private TaskFactory taskFactory;

    public DefaultToken(Token parent, TaskFactory taskFactory) {
        this.parent = parent;
        this.taskFactory = taskFactory;
    }

    protected TaskFactory getTaskFactory() {
        return taskFactory;
    }

    @Override
    public Token getParent() {
        return parent;
    }

    @Override
    public EventChannel getEventChannel() {
        return eventChannel;
    }

    @Override
    public void finish(Object result) {
        this.result = result;
        nextTask = null;
        finished(result);
        eventChannel.fireEvent(TokenListener.class, eh -> eh.finished());
    }

    @Override
    public void passTo(TaskSelector nextTask) {
        this.nextTask = nextTask;
        wasPassedTo(nextTask);
        eventChannel.fireEvent(TokenListener.class, eh -> eh.wasPassed());
    }

    @Override
    public Token newToken(TaskSelector initialTask) {
        return new DefaultToken(parent, taskFactory);
    }

    public void addSequentialScheduler(Token token) {
        tokens.add(token);
        getEventChannel().fireEvent(TokenContainerListener.class, eh -> eh.addedToken(token));
    }

    private ArrayList<Token> tokens = new ArrayList<>();

    public void proceed() {
        //nextTask.accept(this);
        taskFactory.newTask(nextTask.getName(), nextTask.getArguments()).accept(this);
        //nextTask.newTask().accept(this);
    }

    protected void setNextTask(TaskSelector nextTask) {
        this.nextTask = nextTask;
    }

    public boolean hasMore() {
        return nextTask != null;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public void close() throws Exception {
        tokens.forEach(ss -> {
            try {
                ss.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        wasClosed();
        eventChannel.fireEvent(TokenListener.class, eh -> eh.wasClosed());
    }

    protected void wasPassedTo(TaskSelector nextTask) {

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
