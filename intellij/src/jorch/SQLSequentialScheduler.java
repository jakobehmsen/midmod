package jorch;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SQLSequentialScheduler extends DefaultSequentialScheduler {
    private int id;
    private int parentId;
    private SQLRepository connectionSupplier;

    public SQLSequentialScheduler(int id, int parentId, SQLRepository connectionSupplier) {
        this.id = id;
        this.parentId = parentId;
        this.connectionSupplier = connectionSupplier;
    }

    public static List<SQLSequentialScheduler> all(SQLRepository connectionSupplier) throws SQLException {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM sequential_scheduler WHERE parent_id IS NULL", Statement.RETURN_GENERATED_KEYS)) {
                ResultSet resultSet = statement.executeQuery();
                return all(connectionSupplier, resultSet);
            }
        }
    }

    public static List<SQLSequentialScheduler> all(SQLRepository connectionSupplier, int parentId) throws SQLException {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM sequential_scheduler WHERE parent_id = ?", Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, parentId);
                ResultSet resultSet = statement.executeQuery();
                return all(connectionSupplier, resultSet);
            }
        }
    }

    public static List<SQLSequentialScheduler> all(SQLRepository connectionSupplier, ResultSet resultSet) throws SQLException {
        ArrayList<SQLSequentialScheduler> all = new ArrayList<>();

        while(resultSet.next()) {
            SQLSequentialScheduler ss = single(connectionSupplier, resultSet);
            all.add(ss);
        }

        return all;
    }

    public static SQLSequentialScheduler single(SQLRepository connectionSupplier, ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt(1);
        int concurrentSchedulerId = resultSet.getInt(2);
        SQLSequentialScheduler ss = new SQLSequentialScheduler(id, concurrentSchedulerId, connectionSupplier);
        Blob nextTaskAsBlob = resultSet.getBlob(3);
        if (!resultSet.wasNull()) {
            try (InputStream inputStream = nextTaskAsBlob.getBinaryStream()) {
                try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                    Consumer<SequentialScheduler> nextTask = (Consumer<SequentialScheduler>) objectInputStream.readObject();
                    ss.setNextTask(nextTask);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Blob resultAsBlob = resultSet.getBlob(4);
        if (!resultSet.wasNull()) {
            try (InputStream inputStream = resultAsBlob.getBinaryStream()) {
                try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                    Object result = objectInputStream.readObject();
                    ss.setFinished(result);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<SQLSequentialScheduler> sqlSequentialSchedulers = all(connectionSupplier, id);
        sqlSequentialSchedulers.forEach(ssc -> ss.addSequentialScheduler(ssc));
        return ss;
    }

    public static SQLSequentialScheduler add(SQLRepository connectionSupplier) throws SQLException {
        return add(connectionSupplier, 0);
    }

    public static SQLSequentialScheduler add(SQLRepository connectionSupplier, int parentId) throws SQLException {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO sequential_scheduler (parent_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                if(parentId != 0)
                    statement.setInt(1, parentId);
                else
                    statement.setNull(1, 0);
                statement.executeUpdate();
                ResultSet tableKeys = statement.getGeneratedKeys();

                tableKeys.next();
                int id = tableKeys.getInt(1);
                return new SQLSequentialScheduler(id, parentId, connectionSupplier);
            }
        }
    }

    @Override
    protected void finished(Object result) {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("UPDATE sequential_scheduler SET next_task = NULL, result = ? WHERE id = ?")) {
                Blob resultAsBlob = connection.createBlob();
                try(OutputStream outputStream = resultAsBlob.setBinaryStream(1)) {
                    try(ObjectOutputStream cachedResultObjectOutputStream = new ObjectOutputStream(outputStream)) {
                        cachedResultObjectOutputStream.writeObject(result);
                    }
                }
                statement.setBlob(1, resultAsBlob);
                statement.setInt(2, id);
                statement.executeUpdate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void scheduledNext(Consumer<SequentialScheduler> nextTask) {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("UPDATE sequential_scheduler SET next_task = ? WHERE id = ?")) {
                Blob nextTaskAsBlob = connection.createBlob();
                try(OutputStream outputStream = nextTaskAsBlob.setBinaryStream(1)) {
                    try(ObjectOutputStream cachedResultObjectOutputStream = new ObjectOutputStream(outputStream)) {
                        cachedResultObjectOutputStream.writeObject(nextTask);
                    }
                }
                statement.setBlob(1, nextTaskAsBlob);
                statement.setInt(2, id);
                statement.executeUpdate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void wasClosed() {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM sequential_scheduler WHERE id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SequentialScheduler newSequentialScheduler(Consumer<SequentialScheduler> initialTask) {
        try {
            SQLSequentialScheduler ss = SQLSequentialScheduler.add(connectionSupplier, id);
            addSequentialScheduler(ss);
            return ss;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected ConcurrentScheduler newConcurrentScheduler() {
        try {
            return SQLConcurrentScheduler.add(connectionSupplier, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
