package jorch;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SQLSequentialScheduler extends DefaultSequentialScheduler {
    private int id;
    private int concurrentSchedulerId;
    private Supplier<Connection> connectionSupplier;

    public SQLSequentialScheduler(int id, int concurrentSchedulerId, Supplier<Connection> connectionSupplier) {
        this.id = id;
        this.concurrentSchedulerId = concurrentSchedulerId;
        this.connectionSupplier = connectionSupplier;
    }

    public static List<SQLSequentialScheduler> all(Supplier<Connection> connectionSupplier) throws SQLException {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM sequential_scheduler WHERE concurrent_scheduler_id IS NULL", Statement.RETURN_GENERATED_KEYS)) {
                ArrayList<SQLSequentialScheduler> all = new ArrayList<>();

                try(ResultSet resultSet = statement.executeQuery()) {
                    while(resultSet.next()) {
                        int id = resultSet.getInt(1);
                        int concurrentSchedulerId = resultSet.getInt(2);
                        SQLSequentialScheduler ss =  new SQLSequentialScheduler(id, concurrentSchedulerId, connectionSupplier);
                        Blob nextTaskAsBlob = resultSet.getBlob(3);
                        if(!resultSet.wasNull()) {
                            try(InputStream inputStream = nextTaskAsBlob.getBinaryStream()) {
                                try(ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
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
                        if(!resultSet.wasNull()) {
                            try(InputStream inputStream = resultAsBlob.getBinaryStream()) {
                                try(ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                                    Object result = objectInputStream.readObject();
                                    ss.setFinished(result);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        // Load all related concurrent schedulers; change select to be a join?
                        // Can be loaded lazily?
                        all.add(ss);
                    }
                }

                return all;
            }
        }
    }

    public static SQLSequentialScheduler add(Supplier<Connection> connectionSupplier) throws SQLException {
        return add(connectionSupplier, 0);
    }

    public static SQLSequentialScheduler add(Supplier<Connection> connectionSupplier, int concurrentSchedulerId) throws SQLException {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO sequential_scheduler (concurrent_scheduler_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                if(concurrentSchedulerId != 0)
                    statement.setInt(1, concurrentSchedulerId);
                else
                    statement.setNull(1, 0);
                statement.executeUpdate();
                ResultSet tableKeys = statement.getGeneratedKeys();

                tableKeys.next();
                int id = tableKeys.getInt(1);
                return new SQLSequentialScheduler(id, concurrentSchedulerId, connectionSupplier);
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
    protected ConcurrentScheduler newConcurrentScheduler() {
        try {
            return SQLConcurrentScheduler.add(connectionSupplier, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
