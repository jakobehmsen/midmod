package jorch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

public abstract class TokenRepository {
    private EventChannel eventChannel = new EventChannel();
    private static ThreadLocal<PersistenceSession> rootSession = new ThreadLocal<>();
    private Serializer serializer;
    private TaskFactory taskFactory;

    public TokenRepository(Serializer serializer, TaskFactory taskFactory) {
        this.serializer = serializer;
        this.taskFactory = taskFactory;
    }

    public EventChannel getEventChannel() {
        return eventChannel;
    }

    public RepositoryBasedToken newToken(TaskSelector initialTask) throws SQLException {
        initialTask = load(initialTask);
        RepositoryBasedToken t = addToken(taskFactory);
        t.passTo(initialTask);
        eventChannel.fireEvent(TokenContainerListener.class, eh -> eh.addedToken(t));
        return t;
    }

    protected abstract RepositoryBasedToken addToken(TaskFactory taskFactory) throws SQLException;

    public List<RepositoryBasedToken> allTokens() throws SQLException {
        return allTokens(taskFactory);
    }

    protected abstract List<RepositoryBasedToken> allTokens(TaskFactory taskFactory);

    public TaskSelector load(TaskSelector task) {
        return task;
    }

    public void serialize(Object object, OutputStream outputStream) throws IOException {
        serializer.serialize(object, outputStream);
    }

    public Object deserialize(InputStream inputStream) throws ClassNotFoundException, IOException {
        return serializer.deserialize(inputStream);
    }

    public RepositoryBasedToken addToken(RepositoryBasedToken parent) throws SQLException {
        return addToken(parent, taskFactory);
    }

    protected abstract RepositoryBasedToken addToken(RepositoryBasedToken parent, TaskFactory taskFactory) throws SQLException;

    public abstract void finished(RepositoryBasedToken token, Object result);

    public abstract void wasPassedTo(RepositoryBasedToken token, TaskSelector nextTask);

    public abstract void close(RepositoryBasedToken token);

    public abstract boolean exists();

    public abstract void create();
}
