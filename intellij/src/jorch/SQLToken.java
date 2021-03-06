package jorch;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SQLToken extends DefaultToken {
    private int id;
    private SQLRepository repository;
    private Queue<SQLToken> waitingFor;

    public SQLToken(int id, SQLToken parent, TaskFactory taskFactory, SQLRepository connectionSupplier) {
        super(parent, taskFactory);
        this.id = id;
        this.repository = connectionSupplier;
    }

    @Override
    public SQLToken getParent() {
        return (SQLToken) super.getParent();
    }

    public static List<SQLToken> all(SQLRepository connectionSupplier, TaskFactory taskFactory) throws SQLException {
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

    public static List<SQLToken> all(SQLRepository connectionSupplier, SQLToken parent, TaskFactory taskFactory) throws SQLException {
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

    public static List<SQLToken> all(SQLRepository connectionSupplier, SQLToken parent, TaskFactory taskFactory, ResultSet resultSet) throws SQLException {
        ArrayList<SQLToken> all = new ArrayList<>();

        while(resultSet.next()) {
            SQLToken t = single(connectionSupplier, parent, taskFactory, resultSet);
            all.add(t);
        }

        return all;
    }

    public static SQLToken single(SQLRepository connectionSupplier, SQLToken parent, TaskFactory taskFactory, ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt(1);
        SQLToken t = new SQLToken(id, parent, taskFactory, connectionSupplier);
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
        List<SQLToken> sqlSequentialSchedulers = all(connectionSupplier, t, taskFactory);
        t.waitingFor = new LinkedList<>(sqlSequentialSchedulers);
        return t;
    }

    public static SQLToken add(SQLRepository connectionSupplier, TaskFactory taskFactory) throws SQLException {
        return add(connectionSupplier, null, taskFactory);
    }

    public static SQLToken add(SQLRepository connectionSupplier, SQLToken parent, TaskFactory taskFactory) throws SQLException {
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
                return new SQLToken(id, parent, taskFactory, connectionSupplier);
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

        try(PersistenceSession session = repository.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("UPDATE token SET next_task = NULL, result = ? WHERE id = ?")) {
                Blob resultAsBlob = session.getConnection().createBlob();
                try(OutputStream outputStream = resultAsBlob.setBinaryStream(1)) {
                    repository.serialize(result, outputStream);
                }
                statement.setBlob(1, resultAsBlob);
                statement.setInt(2, id);
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
    protected void wasPassedTo(TaskSelector nextTask) {
        try(PersistenceSession session = repository.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("UPDATE token SET next_task = ? WHERE id = ?")) {
                Blob nextTaskAsBlob = session.getConnection().createBlob();
                try(OutputStream outputStream = nextTaskAsBlob.setBinaryStream(1)) {
                    repository.serialize(nextTask, outputStream);
                }
                statement.setBlob(1, nextTaskAsBlob);
                statement.setInt(2, id);
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

            try(PreparedStatement statement = session.getConnection().prepareStatement("DELETE FROM token WHERE id = ?")) {
                statement.setInt(1, id);
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

    @Override
    public Token newToken(TaskSelector initialTask) {
        try {
            SQLToken t;
            if(waitingFor != null && waitingFor.size() > 0)
                t = waitingFor.poll();
            else {
                t = SQLToken.add(repository, this, getTaskFactory());
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
}
