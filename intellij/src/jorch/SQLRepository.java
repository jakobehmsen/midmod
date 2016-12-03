package jorch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private EventChannel eventChannel = new EventChannel();
    private static ThreadLocal<SQLSession> rootSession = new ThreadLocal<>();
    private Serializer serializer;

    public SQLRepository(Serializer serializer) {
        this.serializer = serializer;
    }

    public EventChannel getEventChannel() {
        return eventChannel;
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

    public SQLToken newToken(Consumer<Token> initialTask) throws SQLException {
        initialTask = load(initialTask);
        SQLToken t = SQLToken.add(this);
        t.passTo(initialTask);
        eventChannel.fireEvent(TokenContainerListener.class, eh -> eh.addedToken(t));
        return t;
    }

    public List<SQLToken> allTokens() throws SQLException {
        return SQLToken.all(this);
    }

    public Consumer<Token> load(Consumer<Token> task) {
        return task;
    }

    public void serialize(Object object, OutputStream outputStream) throws IOException {
        serializer.serialize(object, outputStream);
    }

    public Object deserialize(InputStream inputStream) throws ClassNotFoundException, IOException {
        return serializer.deserialize(inputStream);
    }
}
