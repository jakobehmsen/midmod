package jorch;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MySQLRepository extends SQLBasedRepository {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the classpath!", e);
        }
    }

    public MySQLRepository(Serializer serializer, TaskFactory taskFactory) {
        super(serializer, taskFactory);
    }

    @Override
    protected Connection newConnection() {
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

    public SQLBasedToken single(SQLBasedToken parent, TaskFactory taskFactory, ResultSet resultSet) throws SQLException {
        int id = getId(resultSet);
        SQLBasedToken t = newToken(id, parent, taskFactory, this);
        Blob nextTaskAsBlob = gextTaskAsBlob(resultSet);
        if (!resultSet.wasNull()) {
            try (InputStream inputStream = nextTaskAsBlob.getBinaryStream()) {
                TaskSelector nextTask = (TaskSelector) deserialize(inputStream);
                t.setNextTask(nextTask);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        Blob resultAsBlob = getResultAsBlob(resultSet);
        if (!resultSet.wasNull()) {
            try (InputStream inputStream = resultAsBlob.getBinaryStream()) {
                try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                    Object result = objectInputStream.readObject();
                    t.setFinished(result);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<SQLBasedToken> sqlSequentialSchedulers = all(t, taskFactory);
        t.setWaitingFor(new LinkedList<>(sqlSequentialSchedulers));
        return t;
    }

    protected SQLBasedToken newToken(int id, SQLBasedToken parent, TaskFactory taskFactory, SQLBasedRepository repository) {
        return new SQLBasedToken(id, parent, taskFactory, repository);
    }

    protected int getId(ResultSet resultSet) throws SQLException {
        return resultSet.getInt(1);
    }

    protected Blob gextTaskAsBlob(ResultSet resultSet) throws SQLException {
        return resultSet.getBlob(3);
    }

    protected Blob getResultAsBlob(ResultSet resultSet) throws SQLException {
        return resultSet.getBlob(4);
    }

    private List<SQLBasedToken> all(SQLBasedToken parent, TaskFactory taskFactory) {
        try(PersistenceSession session = newSession()) {
            if(parent == null) {
                ResultSet resultSet = queryAllRootTokens(session.getConnection());
                return all(null, taskFactory, resultSet);
            } else {
                ResultSet resultSet = queryAllChildTokens(session.getConnection(), parent.getId());
                return all(parent, taskFactory, resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected ResultSet queryAllRootTokens(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM token WHERE parent_id IS NULL", Statement.RETURN_GENERATED_KEYS)) {
            return statement.executeQuery();
        }
    }

    protected ResultSet queryAllChildTokens(Connection connection, int parentId) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM token WHERE parent_id = ?", Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, parentId);
            return statement.executeQuery();
        }
    }

    public List<SQLBasedToken> all(SQLBasedToken parent, TaskFactory taskFactory, ResultSet resultSet) throws SQLException {
        ArrayList<SQLBasedToken> all = new ArrayList<>();

        while(resultSet.next()) {
            SQLBasedToken t = single(parent, taskFactory, resultSet);
            all.add(t);
        }

        return all;
    }

    @Override
    protected SQLBasedToken addToken(TaskFactory taskFactory) throws SQLException {
        return addToken(null, taskFactory);
    }

    @Override
    protected List<SQLBasedToken> allTokens(TaskFactory taskFactory) {
        return null;
    }

    @Override
    protected SQLBasedToken addToken(SQLBasedToken parent, TaskFactory taskFactory) throws SQLException {
        try(PersistenceSession session = newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("INSERT INTO token (parent_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                if(parent != null)
                    statement.setInt(1, parent.getId());
                else
                    statement.setNull(1, 0);
                statement.executeUpdate();
                ResultSet tableKeys = statement.getGeneratedKeys();

                tableKeys.next();
                int id = tableKeys.getInt(1);
                return new SQLBasedToken(id, parent, taskFactory, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void finished(SQLBasedToken token, Object result) {
        try(PersistenceSession session = newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("UPDATE token SET next_task = NULL, result = ? WHERE id = ?")) {
                Blob resultAsBlob = session.getConnection().createBlob();
                try(OutputStream outputStream = resultAsBlob.setBinaryStream(1)) {
                    serialize(result, outputStream);
                }
                statement.setBlob(1, resultAsBlob);
                statement.setInt(2, token.getId());
                statement.executeUpdate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void wasPassedTo(SQLBasedToken token, TaskSelector nextTask) {
        try(PersistenceSession session = newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("UPDATE token SET next_task = ? WHERE id = ?")) {
                Blob nextTaskAsBlob = session.getConnection().createBlob();
                try(OutputStream outputStream = nextTaskAsBlob.setBinaryStream(1)) {
                    serialize(nextTask, outputStream);
                }
                statement.setBlob(1, nextTaskAsBlob);
                statement.setInt(2, token.getId());
                statement.executeUpdate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close(SQLBasedToken token) {
        try(PersistenceSession session = newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("DELETE FROM token WHERE id = ?")) {
                statement.setInt(1, token.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
