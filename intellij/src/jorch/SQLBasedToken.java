package jorch;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SQLBasedToken extends DefaultToken {
    private int id;
    private SQLBasedRepository repository;
    private Queue<SQLBasedToken> waitingFor;

    public SQLBasedToken(int id, SQLBasedToken parent, TaskFactory taskFactory, SQLBasedRepository connectionSupplier) {
        super(parent, taskFactory);
        this.id = id;
        this.repository = connectionSupplier;
    }

    public void setWaitingFor(Queue<SQLBasedToken> waitingFor) {
        this.waitingFor = waitingFor;
    }

    @Override
    public SQLBasedToken getParent() {
        return (SQLBasedToken) super.getParent();
    }

    public static List<SQLBasedToken> all(SQLBasedRepository connectionSupplier, TaskFactory taskFactory) throws SQLException {
        try(PersistenceSession session = connectionSupplier.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("SELECT * FROM token WHERE parent_id IS NULL", Statement.RETURN_GENERATED_KEYS)) {
                ResultSet resultSet = statement.executeQuery();
                return all(connectionSupplier, null, taskFactory, resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<SQLBasedToken> all(SQLBasedRepository connectionSupplier, SQLBasedToken parent, TaskFactory taskFactory) throws SQLException {
        try(PersistenceSession session = connectionSupplier.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("SELECT * FROM token WHERE parent_id = ?", Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, parent.id);
                ResultSet resultSet = statement.executeQuery();
                return all(connectionSupplier, parent, taskFactory, resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<SQLBasedToken> all(SQLBasedRepository connectionSupplier, SQLBasedToken parent, TaskFactory taskFactory, ResultSet resultSet) throws SQLException {
        ArrayList<SQLBasedToken> all = new ArrayList<>();

        while(resultSet.next()) {
            SQLBasedToken t = single(connectionSupplier, parent, taskFactory, resultSet);
            all.add(t);
        }

        return all;
    }

    public static SQLBasedToken single(SQLBasedRepository connectionSupplier, SQLBasedToken parent, TaskFactory taskFactory, ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt(1);
        SQLBasedToken t = new SQLBasedToken(id, parent, taskFactory, connectionSupplier);
        Blob nextTaskAsBlob = resultSet.getBlob(3);
        if (!resultSet.wasNull()) {
            try (InputStream inputStream = nextTaskAsBlob.getBinaryStream()) {
                TaskSelector nextTask = (TaskSelector) connectionSupplier.deserialize(inputStream);
                t.setNextTask(nextTask);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        Blob resultAsBlob = resultSet.getBlob(4);
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
        List<SQLBasedToken> sqlSequentialSchedulers = all(connectionSupplier, t, taskFactory);
        t.waitingFor = new LinkedList<>(sqlSequentialSchedulers);
        return t;
    }

    public static SQLBasedToken add(SQLBasedRepository connectionSupplier, TaskFactory taskFactory) throws SQLException {
        return add(connectionSupplier, null, taskFactory);
    }

    public static SQLBasedToken add(SQLBasedRepository connectionSupplier, SQLBasedToken parent, TaskFactory taskFactory) throws SQLException {
        try(PersistenceSession session = connectionSupplier.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("INSERT INTO token (parent_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                if(parent != null)
                    statement.setInt(1, parent.id);
                else
                    statement.setNull(1, 0);
                statement.executeUpdate();
                ResultSet tableKeys = statement.getGeneratedKeys();

                tableKeys.next();
                int id = tableKeys.getInt(1);
                return new SQLBasedToken(id, parent, taskFactory, connectionSupplier);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void finished(Object result) {
        if(getParent() == null) {
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        repository.finished(this, result);
    }

    @Override
    protected void wasPassedTo(TaskSelector nextTask) {
        repository.wasPassedTo(this, nextTask);
    }

    @Override
    protected void wasClosed() {
        try(PersistenceSession session = repository.newSession()) {
            if(waitingFor != null) {
                waitingFor.forEach(x -> {
                    try {
                        x.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            repository.close(this);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Token newToken(TaskSelector initialTask) {
        try {
            SQLBasedToken t;
            if(waitingFor != null && waitingFor.size() > 0)
                t = waitingFor.poll();
            else {
                t = repository.addToken(this);
                t.passTo(initialTask);
            }
            addSequentialScheduler(t);
            return t;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isWaiting() {
        return waitingFor != null && waitingFor.size() > 0;
    }

    public int getId() {
        return id;
    }
}
