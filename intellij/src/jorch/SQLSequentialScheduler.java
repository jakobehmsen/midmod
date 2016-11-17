package jorch;

import java.io.*;
import java.sql.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SQLSequentialScheduler extends DefaultSequentialScheduler {
    private int id;
    private Supplier<Connection> connectionSupplier;

    public SQLSequentialScheduler(int id, Supplier<Connection> connectionSupplier) {
        this.id = id;
        this.connectionSupplier = connectionSupplier;
    }

    public static SQLSequentialScheduler insert(Supplier<Connection> connectionSupplier) throws SQLException {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO sequential_scheduler VALUES ()", Statement.RETURN_GENERATED_KEYS)) {
                statement.executeUpdate();
                ResultSet tableKeys = statement.getGeneratedKeys();
                tableKeys.next();
                int id = tableKeys.getInt(1);
                return new SQLSequentialScheduler(id, connectionSupplier);
            }
        }
    }

    @Override
    protected void finished(Object result) {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("UPDATE sequential_scheduler SET cached_result = ? WHERE id = ?")) {
                Blob resultAsBlob = connection.createBlob();
                OutputStream outputStream = resultAsBlob.setBinaryStream(1);
                ObjectOutputStream cachedResultObjectOutputStream = new ObjectOutputStream(outputStream);
                cachedResultObjectOutputStream.writeObject(result);
                statement.setBlob(0, resultAsBlob);
                statement.setInt(1, id);
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
                OutputStream outputStream = nextTaskAsBlob.setBinaryStream(1);
                ObjectOutputStream cachedResultObjectOutputStream = new ObjectOutputStream(outputStream);
                cachedResultObjectOutputStream.writeObject(nextTask);
                statement.setBlob(0, nextTaskAsBlob);
                statement.setInt(1, id);
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
            try(PreparedStatement statement = connection.prepareStatement("DELETE sequential_scheduler WHERE id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ConcurrentScheduler newConcurrentScheduler() {
        return SQLConcurrentScheduler.insert(connectionSupplier);
    }
}
