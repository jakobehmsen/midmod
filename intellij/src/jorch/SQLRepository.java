package jorch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

public class SQLRepository implements Supplier<Connection> {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the classpath!", e);
        }
    }

    @Override
    public Connection get() {
        String url = "jdbc:mysql://localhost:3306/jorch";
        String username = "jorch_user";
        String password = "12345678";

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SQLSequentialScheduler newSequentialScheduler() throws SQLException {
        return SQLSequentialScheduler.add(this);
    }

    public List<SQLSequentialScheduler> allSequentialSchedulers() throws SQLException {
        return SQLSequentialScheduler.all(this);
    }
}
