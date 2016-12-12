package jorch;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MySQLTokenRepository extends TokenRepository {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the classpath!", e);
        }
    }

    public MySQLTokenRepository(Serializer serializer, TaskFactory taskFactory) {
        super(serializer, taskFactory);
    }

    private String schemaName = "jorch2";

    private Connection newConnection() {
        return newConnection(schemaName);
    }

    private Connection newConnection(String schemaName) {
        String url = "jdbc:mysql://localhost:3306/" + schemaName + "?autoReconnect=true&useSSL=false";
        String username = "jorch_user";
        String password = "12345678";

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean exists() {
        try(Connection connection = newConnection("INFORMATION_SCHEMA")) {
            try(Statement statement = connection.createStatement()) {
                String query = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + schemaName + "'";
                ResultSet rs = statement.executeQuery(query);
                rs.next();
                return rs.getInt("COUNT(*)") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void create() {
        try(Connection connection = newConnection("INFORMATION_SCHEMA")) {
            try(Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE " + schemaName + "");
                statement.execute("USE " + schemaName);
                String sql =
                    "CREATE TABLE `token` (\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `parent_id` int(11) DEFAULT NULL,\n" +
                        "  `next_task` blob,\n" +
                        "  `result` blob,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  KEY `fk_parent_idx` (`parent_id`),\n" +
                        "  CONSTRAINT `fk_parent` FOREIGN KEY (`parent_id`) REFERENCES `token` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION\n" +
                        ") ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=latin1;\n" +
                        "";
                statement.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public RepositoryBasedToken single(RepositoryBasedToken parent, TaskFactory taskFactory, ResultSet resultSet) throws SQLException {
        int id = getId(resultSet);
        RepositoryBasedToken t = newToken(id, parent, taskFactory, this);
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
        List<RepositoryBasedToken> sqlSequentialSchedulers = all(t, taskFactory);
        t.setWaitingFor(new LinkedList<>(sqlSequentialSchedulers));
        return t;
    }

    protected RepositoryBasedToken newToken(int id, RepositoryBasedToken parent, TaskFactory taskFactory, TokenRepository repository) {
        return new RepositoryBasedToken(id, parent, taskFactory, repository);
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

    private List<RepositoryBasedToken> all(RepositoryBasedToken parent, TaskFactory taskFactory) {
        try(Connection connection = newConnection()) {
            if(parent == null) {
                return queryAllRootTokens(connection, taskFactory);
            } else {
                return queryAllChildTokens(connection, parent, taskFactory);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected List<RepositoryBasedToken> queryAllRootTokens(Connection connection, TaskFactory taskFactory) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM token WHERE parent_id IS NULL", Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = statement.executeQuery();
            return all(null, taskFactory, resultSet);
        }
    }

    protected List<RepositoryBasedToken> queryAllChildTokens(Connection connection, RepositoryBasedToken parent, TaskFactory taskFactory) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM token WHERE parent_id = ?", Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, parent.getId());
            ResultSet resultSet = statement.executeQuery();
            return all(parent, taskFactory, resultSet);
        }
    }

    public List<RepositoryBasedToken> all(RepositoryBasedToken parent, TaskFactory taskFactory, ResultSet resultSet) throws SQLException {
        ArrayList<RepositoryBasedToken> all = new ArrayList<>();

        while(resultSet.next()) {
            RepositoryBasedToken t = single(parent, taskFactory, resultSet);
            all.add(t);
        }

        return all;
    }

    @Override
    protected RepositoryBasedToken addToken(TaskFactory taskFactory) throws SQLException {
        return addToken(null, taskFactory);
    }

    @Override
    protected List<RepositoryBasedToken> allTokens(TaskFactory taskFactory) {
        return all(null, taskFactory);
    }

    @Override
    protected RepositoryBasedToken addToken(RepositoryBasedToken parent, TaskFactory taskFactory) throws SQLException {
        try(Connection connection = newConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO token (parent_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                if(parent != null)
                    statement.setInt(1, parent.getId());
                else
                    statement.setNull(1, 0);
                statement.executeUpdate();
                ResultSet tableKeys = statement.getGeneratedKeys();

                tableKeys.next();
                int id = tableKeys.getInt(1);
                return new RepositoryBasedToken(id, parent, taskFactory, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void finished(RepositoryBasedToken token, Object result) {
        try(Connection connection = newConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("UPDATE token SET next_task = NULL, result = ? WHERE id = ?")) {
                Blob resultAsBlob = connection.createBlob();
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
    public void wasPassedTo(RepositoryBasedToken token, TaskSelector nextTask) {
        try(Connection connection = newConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("UPDATE token SET next_task = ? WHERE id = ?")) {
                Blob nextTaskAsBlob = connection.createBlob();
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
    public void close(RepositoryBasedToken token) {
        try(Connection connection = newConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM token WHERE id = ?")) {
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
