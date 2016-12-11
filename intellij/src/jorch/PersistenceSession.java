package jorch;

import java.sql.Connection;

public interface PersistenceSession extends AutoCloseable {
    Connection getConnection();

    void attach();
    void detach();
}
