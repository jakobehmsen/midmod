package jorch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class SQLBasedRepository {
    private EventChannel eventChannel = new EventChannel();
    private static ThreadLocal<PersistenceSession> rootSession = new ThreadLocal<>();
    private Serializer serializer;
    private TaskFactory taskFactory;

    public SQLBasedRepository(Serializer serializer, TaskFactory taskFactory) {
        this.serializer = serializer;
        this.taskFactory = taskFactory;
    }

    public EventChannel getEventChannel() {
        return eventChannel;
    }

    public PersistenceSession newSession() {
        if (rootSession.get() == null) {
            PersistenceSession session = new PersistenceSession() {
                private Connection connection;

                {
                    connection = newConnection();
                    try {
                        connection.setAutoCommit(false);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public Connection getConnection() {
                    return connection;
                }

                @Override
                public void attach() {
                    rootSession.set(this);
                }

                @Override
                public void detach() {
                    rootSession.set(null);
                }

                @Override
                public void close() throws Exception {
                    connection.commit();
                    connection.close();
                    rootSession.set(null);
                }
            };
            rootSession.set(session);
            return session;
        }

        return new PersistenceSession() {
            private PersistenceSession rootSession = SQLBasedRepository.rootSession.get();

            @Override
            public Connection getConnection() {
                return rootSession.getConnection();
            }

            @Override
            public void attach() {
                rootSession.attach();
            }

            @Override
            public void detach() {
                rootSession.detach();;
            }

            @Override
            public void close() throws Exception {

            }
        };
    }

    protected abstract Connection newConnection();

    public SQLBasedToken newToken(TaskSelector initialTask) throws SQLException {
        initialTask = load(initialTask);
        SQLBasedToken t = addToken(taskFactory);
        t.passTo(initialTask);
        eventChannel.fireEvent(TokenContainerListener.class, eh -> eh.addedToken(t));
        return t;
    }

    protected abstract SQLBasedToken addToken(TaskFactory taskFactory) throws SQLException;

    public List<SQLBasedToken> allTokens() throws SQLException {
        return allTokens(taskFactory);
    }

    protected abstract List<SQLBasedToken> allTokens(TaskFactory taskFactory);

    public TaskSelector load(TaskSelector task) {
        return task;
    }

    public void serialize(Object object, OutputStream outputStream) throws IOException {
        serializer.serialize(object, outputStream);
    }

    public Object deserialize(InputStream inputStream) throws ClassNotFoundException, IOException {
        return serializer.deserialize(inputStream);
    }

    public SQLBasedToken addToken(SQLBasedToken parent) throws SQLException {
        return addToken(parent, taskFactory);
    }

    protected abstract SQLBasedToken addToken(SQLBasedToken parent, TaskFactory taskFactory) throws SQLException;

    public abstract void finished(SQLBasedToken token, Object result);

    public abstract void wasPassedTo(SQLBasedToken token, TaskSelector nextTask);

    public abstract void close(SQLBasedToken token);
}
