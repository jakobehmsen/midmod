package jorch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Main {
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static DefaultListModel<Runnable> tasksModel;

    private static void requestHalt(Runnable activator) {
        SwingUtilities.invokeLater(() -> {
            tasksModel.addElement(activator);
        });
    }

    private static <T> T requestHaltCall(Callable<T> activator) {
        CountDownLatch latch = new CountDownLatch(1);
        T[] resultHolder = (T[])new Object[1];

        Future<T> future = executorService.submit(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return resultHolder[0];
        });

        SwingUtilities.invokeLater(() -> {
            tasksModel.addElement(new Runnable() {
                @Override
                public void run() {
                    try {
                        resultHolder[0] = activator.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                }

                @Override
                public String toString() {
                    return activator.toString();
                }
            });
        });

        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
        TaskFactory taskFactory = new ReflectiveTaskFactory(token -> new Scheduler(token, executorService),
            new TestTaskFactory(a -> requestHalt(a), c -> requestHaltCall(c)));

        //SQLRepository repository = new SQLRepository(new MigratingSerializer(), taskFactory);
        MySQLTokenRepository repository = new MySQLTokenRepository(new MigratingSerializer(), taskFactory);

        if(!repository.exists()) {
            repository.create();
        }

        ArrayList<Supplier<TaskSelector>> procedures = new ArrayList<>();

        procedures.add(new Supplier<TaskSelector>() {
            @Override
            public TaskSelector get() {
                return new TaskSelector("forkAndMerge", new Object[]{});
            }

            @Override
            public String toString() {
                return "Procedure fork-and-merge";
            }
        });

        DefaultListModel<Supplier<TaskSelector>> proceduresModel = new DefaultListModel<>();
        JList<Supplier<TaskSelector>> proceduresView = new JList<>(proceduresModel);
        procedures.forEach(p -> proceduresModel.addElement(p));
        proceduresView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && proceduresView.getSelectedIndex() != -1) {
                    Supplier<TaskSelector> procedure = proceduresView.getSelectedValue();
                    TaskSelector taskSelector = procedure.get();

                    try {
                        RepositoryBasedToken token = repository.newToken(taskSelector);
                        executorService.execute(() -> {
                            token.proceed();
                        });

                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        JFrame f = new JFrame();
        JPanel contentPane2 = (JPanel) f.getContentPane();

        JScrollPane proceduresViewScrollPane = new JScrollPane(proceduresView);
        proceduresViewScrollPane.setBorder(BorderFactory.createTitledBorder("Procedures"));
        contentPane2.add(proceduresViewScrollPane, BorderLayout.WEST);

        tasksModel = new DefaultListModel<>();
        JList<Runnable> tasksView = new JList<>(tasksModel);

        Consumer<Token> addSS = token -> {
            token.getEventChannel().add(new TokenListener() {
                @Override
                public void wasPassed() {
                    executorService.execute(() -> {
                        ((RepositoryBasedToken)token).proceed();
                    });
                }

                @Override
                public void finished() {

                }

                @Override
                public void wasClosed() {

                }
            });
        };

        BiFunction<Token, Boolean, TokenContainerListener> tokenListenerFunction = new BiFunction<Token, Boolean, TokenContainerListener>() {
            @Override
            public TokenContainerListener apply(Token tokenX, Boolean atLoad) {
                return new TokenContainerListener() {
                    private boolean loading = atLoad;

                    @Override
                    public void addedToken(Token token2) {
                        if(((RepositoryBasedToken)token2).hasMore() || token2.getParent() == null)
                            addSS.accept(token2);

                        token2.getEventChannel().add(this);

                        synchronized (this) {
                            if (loading && ((RepositoryBasedToken)token2).hasMore() && ((RepositoryBasedToken)token2).isWaiting()) {
                                executorService.execute(() -> {
                                    RepositoryBasedToken t = (RepositoryBasedToken) token2;
                                    t.proceed();
                                });

                                loading = false;
                            }
                        }
                    }
                };
            }
        };
        repository.allTokens().forEach(ss -> {
            tokenListenerFunction.apply(ss, true).addedToken(ss);
        });
        tasksView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && tasksView.getSelectedIndex() != -1) {
                    Runnable t = tasksView.getSelectedValue();
                    executorService.execute(() -> {
                        t.run();
                        tasksModel.removeElement(t);
                    });
                }
            }
        });

        repository.getEventChannel().add((TokenContainerListener) token -> {
            addSS.accept(token);

            token.getEventChannel().add(new TokenContainerListener() {
                @Override
                public void addedToken(Token token2) {
                    addSS.accept(token2);

                    token2.getEventChannel().add(this);
                }
            });
        });

        JScrollPane tasksViewScrollPane = new JScrollPane(tasksView);
        tasksViewScrollPane.setBorder(BorderFactory.createTitledBorder("Tasks"));
        contentPane2.add(tasksViewScrollPane, BorderLayout.CENTER);

        f.setSize(640, 480);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
