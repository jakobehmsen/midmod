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
        }

        public Object replace() {
            return new EnterValue_V2(name);
        }

        @Override
        public String toString() {
            return "Enter " + name;
        }

        public static void replaceWith(ClassReplacer classReplacer, String name) {
            classReplacer.replaceWith(EnterValue_V2.class, new Class<?>[]{String.class}, new Object[]{name});
        }
    }

    public static class EnterValue_V2 implements Consumer<Token>, Serializable {
        private static final long serialVersionUID = 279437230392067789L;
        private String name;

        public EnterValue_V2(String name) {
            this.name = name;
        }

        @Override
        public void accept(Token token) {
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
        }

        public Object replace() {
            return new EnterValue_V3(name);
        }

        @Override
        public String toString() {
            return "Enter " + name;
        }

        public static void replaceWith(ClassReplacer classReplacer, String name) {
            classReplacer.replaceWith(EnterValue_V3.class, new Class<?>[]{String.class}, new Object[]{name});
        }
    }

    public static class EnterValue_V3 implements Consumer<Token>, Serializable {
        private static final long serialVersionUID = 279437230392067789L;
        private String name;

        public EnterValue_V3(String name) {
            this.name = name;
        }

        @Override
        public void accept(Token token) {
            requestHalt(new Runnable() {
                @Override
                public void run() {
                    String enterValue = JOptionPane.showInputDialog("Please enter " + name + " :)");

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
            //token.passTo(new Step4());
            token.passTo(new DefaultTaskSupplier(Step4.class));
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
            //TaskFuture<String> s1 = scheduler.call(new EnterValue("Name"));
            //TaskFuture<String> s2 = scheduler.call(new EnterValue("Age"));
            TaskFuture<String> s1 = scheduler.call(new DefaultTaskSupplier(EnterValue.class, "Name"));
            TaskFuture<String> s2 = scheduler.call(new DefaultTaskSupplier(EnterValue.class, "Age"));

            String s1Result = s1.get();
            String s2Result = s2.get();

            System.out.println("Name: " + s1Result);
            System.out.println("Age: " + s2Result);

            System.out.println("Merged and all are finished");

            //token.passTo(new ShowResult("Hello " + s1Result + " (" + s2Result + ")"));
            //token.passTo(new DefaultTaskSupplier(ShowResult.class, "Hello " + s1Result + " (" + s2Result + ")"));
            token.passTo(new DefaultTaskSupplier(ShowResult.class).withArgument(Object.class, "Hello " + s1Result + " (" + s2Result + ")"));
        }

        @Override
        public String toString() {
            return "Fork and merge";
        }
    }

    public static class ShowResult implements Consumer<Token>, Serializable {
        private static final long serialVersionUID = -3458379226568751650L;
        private Object result;

        public ShowResult(Object result) {
            this.result = result;
        }

        @Override
        public void accept(Token token) {
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
        }

        @Override
        public String toString() {
            return "Step 4";
        }
    }

    private static DefaultListModel<Runnable> tasksModel;

    private static void requestHalt(Runnable activator) {
        SwingUtilities.invokeLater(() -> {
            tasksModel.addElement(activator);
        });
    }

    public static void main(String[] args) throws Exception {
        SQLRepository repository = new SQLRepository(new MigratingSerializer());

        ArrayList<TaskSupplier> procedures = new ArrayList<>();

        procedures.add(new DefaultTaskSupplier(Step1.class));
        procedures.add(new DefaultTaskSupplier(Step3ForkAndMerge.class));

        /*procedures.add(new Supplier<Consumer<Token>>() {
            @Override
            public Consumer<Token> get() {
                return new Step1();
            }

            @Override
            public String toString() {
                return "Procedure 1";
            }
        });*/

        /*procedures.add(new Supplier<Consumer<Token>>() {
            @Override
            public Consumer<Token> get() {
                return new Step3ForkAndMerge();
            }

            @Override
            public String toString() {
                return "Procedure fork-and-merge";
            }
        });*/

        DefaultListModel<TaskSupplier> proceduresModel = new DefaultListModel<>();
        JList<TaskSupplier> proceduresView = new JList<>(proceduresModel);
        procedures.forEach(p -> proceduresModel.addElement(p));
        proceduresView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && proceduresView.getSelectedIndex() != -1) {
                    TaskSupplier procedure = proceduresView.getSelectedValue();
                    //Consumer<Token> task = procedures.get();

                    try {
                        SQLToken token = repository.newToken(procedure);
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
