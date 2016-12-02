package jorch;

import java.util.ArrayList;
import java.util.function.Consumer;

public class DefaultToken implements Token {
    private EventChannel eventChannel = new EventChannel();
    private Consumer<Token> nextTask;
    private Object result;
    private Token parent;

    public DefaultToken(Token parent) {
        this.parent = parent;
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
    public void passTo(Consumer<Token> nextTask) {
        this.nextTask = nextTask;
        wasPassedTo(nextTask);
        eventChannel.fireEvent(TokenListener.class, eh -> eh.wasPassed());
    }

    @Override
    public Token newToken(Consumer<Token> initialTask) {
        return new DefaultToken(parent);
    }

    public void addSequentialScheduler(Token token) {
        tokens.add(token);
        getEventChannel().fireEvent(TokenContainerListener.class, eh -> eh.addedToken(token));
    }

    private ArrayList<Token> tokens = new ArrayList<>();

    public void proceed() {
        nextTask.accept(this);
    }

    protected void setNextTask(Consumer<Token> nextTask) {
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

    protected void wasPassedTo(Consumer<Token> nextTask) {

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
