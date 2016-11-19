package jorch;

import java.sql.*;
import java.util.function.Supplier;

public class SQLConcurrentScheduler extends DefaultConcurrentScheduler {
    private int id;
    private SQLRepository connectionSupplier;

    public SQLConcurrentScheduler(int id, SQLRepository connectionSupplier) {
        this.id = id;
        this.connectionSupplier = connectionSupplier;
    }

    public static SQLConcurrentScheduler add(SQLRepository connectionSupplier, int sequentialSchedulerId) throws SQLException {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO concurrent_scheduler (sequential_scheduler_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, sequentialSchedulerId);
                statement.executeUpdate();
                ResultSet tableKeys = statement.getGeneratedKeys();
                tableKeys.next();
                int id = tableKeys.getInt(1);
                return new SQLConcurrentScheduler(id, connectionSupplier);
            }
        }
    }

    @Override
    protected void wasClosed() {
        try(Connection connection = connectionSupplier.get()) {
            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM concurrent_scheduler WHERE id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected DefaultSequentialScheduler newSequentialScheduler() {
        try {
            return SQLSequentialScheduler.add(connectionSupplier, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
