package jorch;

import java.sql.Connection;
import java.util.function.Supplier;

public class SQLConcurrentScheduler extends DefaultConcurrentScheduler {
    public static SQLConcurrentScheduler insert(Supplier<Connection> connectionSupplier) {
        return null;
    }
}
