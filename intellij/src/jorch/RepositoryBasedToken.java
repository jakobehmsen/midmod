package jorch;

import java.sql.*;
import java.util.Queue;

public class RepositoryBasedToken extends DefaultToken {
    private int id;
    private TokenRepository repository;
    private Queue<RepositoryBasedToken> waitingFor;

    public RepositoryBasedToken(int id, RepositoryBasedToken parent, TaskFactory taskFactory, TokenRepository connectionSupplier) {
        super(parent, taskFactory);
        this.id = id;
        this.repository = connectionSupplier;
    }

    public void setWaitingFor(Queue<RepositoryBasedToken> waitingFor) {
        this.waitingFor = waitingFor;
    }

    @Override
    public RepositoryBasedToken getParent() {
        return (RepositoryBasedToken) super.getParent();
    }

    @Override
    protected void finished(Object result) {
        if(getParent() == null) {
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        repository.finished(this, result);
    }

    @Override
    protected void wasPassedTo(TaskSelector nextTask) {
        repository.wasPassedTo(this, nextTask);
    }

    @Override
    protected void wasClosed() {
        if(waitingFor != null) {
            waitingFor.forEach(x -> {
                try {
                    x.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        repository.close(this);
    }

    @Override
    public Token newToken(TaskSelector initialTask) {
        try {
            RepositoryBasedToken t;
            if(waitingFor != null && waitingFor.size() > 0)
                t = waitingFor.poll();
            else {
                t = repository.addToken(this);
                t.passTo(initialTask);
            }
            addSequentialScheduler(t);
            return t;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isWaiting() {
        return waitingFor != null && waitingFor.size() > 0;
    }

    public int getId() {
        return id;
    }
}
