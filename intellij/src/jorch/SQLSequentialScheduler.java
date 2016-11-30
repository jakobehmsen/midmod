package jorch;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class SQLSequentialScheduler extends DefaultSequentialScheduler {
    private int id;
    private SQLRepository connectionSupplier;
    private Queue<SQLSequentialScheduler> waitingFor;

    public SQLSequentialScheduler(int id, SQLSequentialScheduler parent, SQLRepository connectionSupplier) {
        super(parent);
        this.id = id;
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public SQLSequentialScheduler getParent() {
        return (SQLSequentialScheduler) super.getParent();
    }

    public static List<SQLSequentialScheduler> all(SQLRepository connectionSupplier) throws SQLException {
        try(SQLSession session = connectionSupplier.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("SELECT * FROM sequential_scheduler WHERE parent_id IS NULL", Statement.RETURN_GENERATED_KEYS)) {
                ResultSet resultSet = statement.executeQuery();
                return all(connectionSupplier, null, resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<SQLSequentialScheduler> all(SQLRepository connectionSupplier, SQLSequentialScheduler parent) throws SQLException {
        try(SQLSession session = connectionSupplier.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("SELECT * FROM sequential_scheduler WHERE parent_id = ?", Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, parent.id);
                ResultSet resultSet = statement.executeQuery();
                return all(connectionSupplier, parent, resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<SQLSequentialScheduler> all(SQLRepository connectionSupplier, SQLSequentialScheduler parent, ResultSet resultSet) throws SQLException {
        ArrayList<SQLSequentialScheduler> all = new ArrayList<>();

        while(resultSet.next()) {
            SQLSequentialScheduler ss = single(connectionSupplier, parent, resultSet);
            all.add(ss);
        }

        return all;
    }

    public static SQLSequentialScheduler single(SQLRepository connectionSupplier, SQLSequentialScheduler parent, ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt(1);
        SQLSequentialScheduler ss = new SQLSequentialScheduler(id, parent, connectionSupplier);
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
        List<SQLSequentialScheduler> sqlSequentialSchedulers = all(connectionSupplier, ss);
        ss.waitingFor = new LinkedList<>(sqlSequentialSchedulers);
        return ss;
    }

    public static SQLSequentialScheduler add(SQLRepository connectionSupplier) throws SQLException {
        return add(connectionSupplier, null);
    }

    public static SQLSequentialScheduler add(SQLRepository connectionSupplier, SQLSequentialScheduler parent) throws SQLException {
        try(SQLSession session = connectionSupplier.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("INSERT INTO sequential_scheduler (parent_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                if(parent != null)
                    statement.setInt(1, parent.id);
                else
                    statement.setNull(1, 0);
                statement.executeUpdate();
                ResultSet tableKeys = statement.getGeneratedKeys();

                tableKeys.next();
                int id = tableKeys.getInt(1);
                return new SQLSequentialScheduler(id, parent, connectionSupplier);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void finished(Object result) {
        try(SQLSession session = connectionSupplier.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("UPDATE sequential_scheduler SET next_task = NULL, result = ? WHERE id = ?")) {
                Blob resultAsBlob = session.getConnection().createBlob();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void scheduledNext(Consumer<SequentialScheduler> nextTask) {
        try(SQLSession session = connectionSupplier.newSession()) {
            try(PreparedStatement statement = session.getConnection().prepareStatement("UPDATE sequential_scheduler SET next_task = ? WHERE id = ?")) {
                Blob nextTaskAsBlob = session.getConnection().createBlob();
                try(OutputStream outputStream = nextTaskAsBlob.setBinaryStream(1)) {
                    try(ObjectOutputStream cachedResultObjectOutputStream = new ObjectOutputStream(outputStream)) {
                        cachedResultObjectOutputStream.writeObject(nextTask);
                    }
                    catch (NotSerializableException e) {
                        e.toString();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void wasClosed() {
        try(SQLSession session = connectionSupplier.newSession()) {
            if(waitingFor != null) {
                waitingFor.forEach(x -> {
                    try {
                        x.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            try(PreparedStatement statement = session.getConnection().prepareStatement("DELETE FROM sequential_scheduler WHERE id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public SequentialScheduler newSequentialScheduler(Consumer<SequentialScheduler> initialTask) {
        try {
            SQLSequentialScheduler ss;
            if(waitingFor != null && waitingFor.size() > 0)
                ss = waitingFor.poll();
            else {
                ss = SQLSequentialScheduler.add(connectionSupplier, this);
                ss.scheduleNext(initialTask);
            }
            addSequentialScheduler(ss);
            return ss;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isWaiting() {
        return waitingFor != null && waitingFor.size() > 0;
    }
}