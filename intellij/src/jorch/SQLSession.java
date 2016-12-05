package jorch;

import java.sql.Connection;

public interface SQLSession extends AutoCloseable {
    Connection getConnection();

    void attach();
    void detach();
}
