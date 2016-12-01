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

    public static class Step1 implements Consumer<Token>, Serializable {
        @Override
        public void accept(Token token) {
            System.out.println("Step 1 started");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Step 1 ended");
            token.finish(-1);
        }

        private Object readResolve() throws java.io.ObjectStreamException
        {
            return new Step1_2();
        }
    }

    public static class EnterValue implements Consumer<Token>, Serializable {
        private static final long serialVersionUID = 279437230392067789L;
        private String name;

        public EnterValue(String name) {
            this.name = name;
        }

        @Override
        public void accept(Token token) {
            System.out.println("Please enter " + name + ":");
            Scanner s = new Scanner(System.in);
            String enterValue = s.nextLine();
            token.finish(enterValue);
        }

        @Override
        public String toString() {
            return "Enter " + name;
        }
    }

    public static class Step1_2 implements Consumer<Token>, Serializable {
        private static final long serialVersionUID = -5029221089424988655L;

        @Override
        public void accept(Token token) {
            System.out.println("Step 1 started");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Step 1 ended");
            token.finish(-1);
        }

        @Override
        public String toString() {
            return "Step 1 (Version 2)";
        }

        private Object readResolve() throws java.io.ObjectStreamException
        {
            return new Step1_3();
        }
    }

    public static class Step1_3 implements Consumer<Token>, Serializable {
        private static final long serialVersionUID = -5434560544366655728L;

        @Override
        public void accept(Token token) {
            System.out.println("Step 1 started");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Step 1 ended");
            token.passTo(new Step4());
        }

        @Override
        public String toString() {
            return "Step 1 (Version 3)";
        }
    }

    public static class Step4 implements Consumer<Token>, Serializable {
        private static final long serialVersionUID = -6465559582561789121L;

        @Override
        public void accept(Token token) {
            System.out.println("Step 4 started");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Step 4 ended");
            token.finish(new Random().nextInt());
        }

        @Override
        public String toString() {
            return "Step 4";
        }
    }

    public static class Step3ForkAndMerge implements Consumer<Token>, Serializable {
        private static final long serialVersionUID = 7747282616039058003L;

        @Override
        public void accept(Token token) {
            Scheduler scheduler = new Scheduler(token, executorService);
            TaskFuture<String> s1 = scheduler.call(new EnterValue("Name"));
            TaskFuture<String> s2 = scheduler.call(new EnterValue("Age"));

            String s1Result = s1.get();
            String s2Result = s2.get();

            System.out.println("Name: " + s1Result);
            System.out.println("Age: " + s2Result);

            System.out.println("Merged and all are finished");
            token.finish("Hello " + s1Result + " (" + s2Result + ")");
        }

        @Override
        public String toString() {
            return "Fork and merge";
        }
    }

    public static void main(String[] args) throws Exception {
        SQLRepository repository = new SQLRepository(new LoadStrategy() {
            @Override
            public Consumer<Token> load(Consumer<Token> task) {
                return task;
            }
        });

        ArrayList<Supplier<Consumer<Token>>> procedures = new ArrayList<>();

        procedures.add(new Supplier<Consumer<Token>>() {
            @Override
            public Consumer<Token> get() {
                return new Step1();
            }

            @Override
            public String toString() {
                return "Procedure 1";
            }
        });

        procedures.add(new Supplier<Consumer<Token>>() {
            @Override
            public Consumer<Token> get() {
                return new Step3ForkAndMerge();
            }

            @Override
            public String toString() {
                return "Procedure fork-and-merge";
            }
        });

        DefaultListModel<Supplier<Consumer<Token>>> proceduresModel = new DefaultListModel<>();
        JList<Supplier<Consumer<Token>>> proceduresView = new JList<>(proceduresModel);
        procedures.forEach(p -> proceduresModel.addElement(p));
        proceduresView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && proceduresView.getSelectedIndex() != -1) {
                    Supplier<Consumer<Token>> procedures = proceduresView.getSelectedValue();
                    Consumer<Token> task = procedures.get();
                    try {
                        repository.newToken(task);
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

        DefaultListModel<Token> tasksModel = new DefaultListModel<>();
        JList<Token> tasksView = new JList<>(tasksModel);

        Consumer<Token> addSS = token -> {
            tasksModel.addElement(token);
            System.out.println("Added " + token);
            token.getEventChannel().add(new TokenListener() {
                @Override
                public void wasPassed() {
                    tasksModel.set(tasksModel.indexOf(token), token);
                }

                @Override
                public void finished() {
                    //tasksModel.set(tasksModel.indexOf(sequentialScheduler), sequentialScheduler);
                }

                @Override
                public void wasClosed() {
                    tasksModel.removeElement(token);
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
                                    tasksModel.removeElement(t);
                                    t.proceed();
                                    if (t.getParent() == null || t.hasMore())
                                        tasksModel.addElement(t);
                                    /*else {
                                        try {
                                            ss.close();
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }*/
                                });

                                loading = false;
                            }
                        }
                    }
                };
            }
        };
        /*Consumer<SequentialScheduler> loadSS = sequentialScheduler -> {
            addSS.accept(sequentialScheduler);

            sequentialScheduler.getEventChannel().add(sequentialSchedulerEventHandlerFunction.apply(sequentialScheduler, true));

            executorService.execute(() -> {
                SQLSequentialScheduler ss = (SQLSequentialScheduler)sequentialScheduler;
                tasksModel.removeElement(ss);
                ss.proceed();
                if(ss.getParent() == null || ss.hasMore())
                    tasksModel.addElement(ss);
            });
        };*/
        repository.allTokens().forEach(ss -> {
            tokenListenerFunction.apply(ss, true).addedToken(ss);
        });
        tasksView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && tasksView.getSelectedIndex() != -1) {
                    SQLToken t = (SQLToken) tasksView.getSelectedValue();
                    if(t.hasMore()) {
                        executorService.execute(() -> {
                            tasksModel.removeElement(t);
                            t.proceed();
                            if(t.getParent() == null || t.hasMore())
                                tasksModel.addElement(t);
                            /*else {
                                try {
                                    ss.close();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }*/
                        });
                    } else {
                        try {
                            t.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
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
