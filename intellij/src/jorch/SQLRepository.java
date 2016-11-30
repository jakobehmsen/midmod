package jorch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class SQLRepository {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the classpath!", e);
        }
    }

    private LoadStrategy loadStrategy;
    private EventHandlerContainer eventHandlerContainer = new EventHandlerContainer();
    private static ThreadLocal<SQLSession> rootSession = new ThreadLocal<>();

    public SQLRepository(LoadStrategy loadStrategy) {
        this.loadStrategy = loadStrategy;
    }

    public EventHandlerContainer getEventHandlerContainer() {
        return eventHandlerContainer;
    }

    public SQLSession newSession() {
        if (rootSession.get() == null) {
            SQLSession session = new SQLSession() {
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
                public void close() throws Exception {
                    connection.commit();
                    connection.close();
                    rootSession.set(null);
                }
            };
            rootSession.set(session);
            return session;
        }

        return new SQLSession() {
            @Override
            public Connection getConnection() {
                return rootSession.get().getConnection();
            }

            @Override
            public void close() throws Exception {

            }
        };
    }

    private Connection newConnection() {
        String url = "jdbc:mysql://localhost:3306/jorch?autoReconnect=true&useSSL=false";
        String username = "jorch_user";
        String password = "12345678";

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SQLSequentialScheduler newSequentialScheduler(Consumer<SequentialScheduler> initialTask) throws SQLException {
        initialTask = load(initialTask);
        SQLSequentialScheduler ss = SQLSequentialScheduler.add(this);
        ss.scheduleNext(initialTask);
        eventHandlerContainer.fireEvent(SequentialSchedulerContainerEventHandler.class, eh -> eh.addedSequentialScheduler(ss));
        return ss;
    }

    public List<SQLSequentialScheduler> allSequentialSchedulers() throws SQLException {
        return SQLSequentialScheduler.all(this);
    }

    public Consumer<SequentialScheduler> load(Consumer<SequentialScheduler> task) {
        return loadStrategy.load(task);
    }
}
