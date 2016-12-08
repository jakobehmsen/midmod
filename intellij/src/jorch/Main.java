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

    public static void main(String[] args) throws Exception {
        DefaultTaskFactory taskFactory = new DefaultTaskFactory();
        TaskFactoryExt taskFactoryExt = new TaskFactoryExt(taskFactory, token -> new Scheduler(token, executorService));

        taskFactoryExt.mapSplit("Fork-and-merge", arguments -> (token, scheduler) -> {
            TaskFuture<String> s1 = scheduler.call(new TaskSelector("EnterValue", new Object[]{"Name"}));
            TaskFuture<String> s2 = scheduler.call(new TaskSelector("EnterValue", new Object[]{"Age"}));

            String s1Result = s1.get();
            String s2Result = s2.get();

            token.passTo(new TaskSelector("ShowResult", new Object[]{"Hello " + s1Result + " (" + s2Result + ")"}));
        });
        taskFactoryExt.mapTask("ShowResult", arguments -> {
            Object result = arguments[0];
            return token -> {
                requestHalt(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            token.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public String toString() {
                        return "=> " + result;
                    }
                });
            };
        });
        taskFactoryExt.mapTask("EnterValue", arguments -> {
            String name = (String) arguments[0];
            return token -> {
                requestHalt(new Runnable() {
                    @Override
                    public void run() {
                        String enterValue = JOptionPane.showInputDialog("Please enter " + name);

                        if(enterValue != null) {
                            token.finish(enterValue);
                        } else {
                            requestHalt(this);
                        }
                    }

                    @Override
                    public String toString() {
                        return "Enter " + name;
                    }
                });
            };
        });

        SQLRepository repository = new SQLRepository(new MigratingSerializer(), taskFactory);

        ArrayList<Supplier<TaskSelector>> procedures = new ArrayList<>();

        procedures.add(new Supplier<TaskSelector>() {
            @Override
            public TaskSelector get() {
                return new TaskSelector("Fork-and-merge", new Object[]{});
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
                        SQLToken token = repository.newToken(taskSelector);
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
                        ((SQLToken)token).proceed();
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
                        if(((SQLToken)token2).hasMore() || token2.getParent() == null)
                            addSS.accept(token2);

                        token2.getEventChannel().add(this);

                        synchronized (this) {
                            if (loading && ((SQLToken)token2).hasMore() && ((SQLToken)token2).isWaiting()) {
                                executorService.execute(() -> {
                                    SQLToken t = (SQLToken) token2;
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
