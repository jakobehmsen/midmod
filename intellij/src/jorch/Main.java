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

    public static class Step1 implements Consumer<SequentialScheduler>, Serializable {
        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            System.out.println("Step 1 started");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Step 1 ended");
            sequentialScheduler.finish(-1);
        }

        private Object readResolve() throws java.io.ObjectStreamException
        {
            return new Step1_2();
        }
    }

    public static class EnterValue implements Consumer<SequentialScheduler>, Serializable {
        private static final long serialVersionUID = 279437230392067789L;
        private String name;

        public EnterValue(String name) {
            this.name = name;
        }

        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            System.out.println("Please enter " + name + ":");
            Scanner s = new Scanner(System.in);
            String enterValue = s.nextLine();
            sequentialScheduler.finish(enterValue);
        }

        @Override
        public String toString() {
            return "Enter " + name;
        }
    }

    public static class Step1_2 implements Consumer<SequentialScheduler>, Serializable {
        private static final long serialVersionUID = -5029221089424988655L;

        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            System.out.println("Step 1 started");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Step 1 ended");
            sequentialScheduler.finish(-1);
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

    public static class Step1_3 implements Consumer<SequentialScheduler>, Serializable {
        private static final long serialVersionUID = -5434560544366655728L;

        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            System.out.println("Step 1 started");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Step 1 ended");
            sequentialScheduler.scheduleNext(new Step4());
        }

        @Override
        public String toString() {
            return "Step 1 (Version 3)";
        }
    }

    public static class Step4 implements Consumer<SequentialScheduler>, Serializable {
        private static final long serialVersionUID = -6465559582561789121L;

        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            System.out.println("Step 4 started");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Step 4 ended");
            sequentialScheduler.finish(new Random().nextInt());
        }

        @Override
        public String toString() {
            return "Step 4";
        }
    }

    public static class Step3ForkAndMerge implements Consumer<SequentialScheduler>, Serializable {
        private static final long serialVersionUID = 7747282616039058003L;

        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            Scheduler scheduler = new Scheduler(sequentialScheduler, executorService);
            TaskFuture<String> s1 = scheduler.call(new EnterValue("Name"));
            TaskFuture<String> s2 = scheduler.call(new EnterValue("Age"));

            String s1Result = s1.get();
            String s2Result = s2.get();

            System.out.println("Name: " + s1Result);
            System.out.println("Age: " + s2Result);

            System.out.println("Merged and all are finished");
            sequentialScheduler.finish("Hello " + s1Result + " (" + s2Result + ")");
        }

        @Override
        public String toString() {
            return "Fork and merge";
        }
    }

    public static void main(String[] args) throws Exception {
        SQLRepository repository = new SQLRepository(new LoadStrategy() {
            @Override
            public Consumer<SequentialScheduler> load(Consumer<SequentialScheduler> task) {
                return task;
            }
        });

        ArrayList<Supplier<Consumer<SequentialScheduler>>> procedures = new ArrayList<>();

        procedures.add(new Supplier<Consumer<SequentialScheduler>>() {
            @Override
            public Consumer<SequentialScheduler> get() {
                return new Step1();
            }

            @Override
            public String toString() {
                return "Procedure 1";
            }
        });

        procedures.add(new Supplier<Consumer<SequentialScheduler>>() {
            @Override
            public Consumer<SequentialScheduler> get() {
                return new Step3ForkAndMerge();
            }

            @Override
            public String toString() {
                return "Procedure fork-and-merge";
            }
        });

        DefaultListModel<Supplier<Consumer<SequentialScheduler>>> proceduresModel = new DefaultListModel<>();
        JList<Supplier<Consumer<SequentialScheduler>>> proceduresView = new JList<>(proceduresModel);
        procedures.forEach(p -> proceduresModel.addElement(p));
        proceduresView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && proceduresView.getSelectedIndex() != -1) {
                    Supplier<Consumer<SequentialScheduler>> procedures = proceduresView.getSelectedValue();
                    Consumer<SequentialScheduler> task = procedures.get();
                    try {
                        repository.newSequentialScheduler(task);
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

        DefaultListModel<SequentialScheduler> tasksModel = new DefaultListModel<>();
        JList<SequentialScheduler> tasksView = new JList<>(tasksModel);

        Consumer<SequentialScheduler> addSS = sequentialScheduler -> {
            tasksModel.addElement(sequentialScheduler);
            System.out.println("Added " + sequentialScheduler);
            sequentialScheduler.getEventHandlerContainer().addEventHandler(new SequentialSchedulerEventHandler() {
                @Override
                public void proceeded() {
                    tasksModel.set(tasksModel.indexOf(sequentialScheduler), sequentialScheduler);
                }

                @Override
                public void finished() {
                    //tasksModel.set(tasksModel.indexOf(sequentialScheduler), sequentialScheduler);
                }

                @Override
                public void wasClosed() {
                    tasksModel.removeElement(sequentialScheduler);
                }
            });
        };

        BiFunction<SequentialScheduler, Boolean, SequentialSchedulerContainerEventHandler> sequentialSchedulerEventHandlerFunction = new BiFunction<SequentialScheduler, Boolean, SequentialSchedulerContainerEventHandler>() {
            @Override
            public SequentialSchedulerContainerEventHandler apply(SequentialScheduler sequentialSchedulerX, Boolean atLoad) {
                return new SequentialSchedulerContainerEventHandler() {
                    private boolean loading = atLoad;

                    @Override
                    public void addedSequentialScheduler(SequentialScheduler sequentialScheduler2) {
                        if(((SQLSequentialScheduler)sequentialScheduler2).hasMore() || sequentialScheduler2.getParent() == null)
                            addSS.accept(sequentialScheduler2);

                        sequentialScheduler2.getEventHandlerContainer().addEventHandler(this);

                        synchronized (this) {
                            if (loading && ((SQLSequentialScheduler)sequentialScheduler2).hasMore() && ((SQLSequentialScheduler)sequentialScheduler2).isWaiting()) {
                                executorService.execute(() -> {
                                    SQLSequentialScheduler ss = (SQLSequentialScheduler) sequentialScheduler2;
                                    tasksModel.removeElement(ss);
                                    ss.proceed();
                                    if (ss.getParent() == null || ss.hasMore())
                                        tasksModel.addElement(ss);
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

            sequentialScheduler.getEventHandlerContainer().addEventHandler(sequentialSchedulerEventHandlerFunction.apply(sequentialScheduler, true));

            executorService.execute(() -> {
                SQLSequentialScheduler ss = (SQLSequentialScheduler)sequentialScheduler;
                tasksModel.removeElement(ss);
                ss.proceed();
                if(ss.getParent() == null || ss.hasMore())
                    tasksModel.addElement(ss);
            });
        };*/
        repository.allSequentialSchedulers().forEach(ss -> {
            sequentialSchedulerEventHandlerFunction.apply(ss, true).addedSequentialScheduler(ss);
        });
        tasksView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && tasksView.getSelectedIndex() != -1) {
                    SQLSequentialScheduler ss = (SQLSequentialScheduler) tasksView.getSelectedValue();
                    if(ss.hasMore()) {
                        executorService.execute(() -> {
                            tasksModel.removeElement(ss);
                            ss.proceed();
                            if(ss.getParent() == null || ss.hasMore())
                                tasksModel.addElement(ss);
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
                            ss.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });

        repository.getEventHandlerContainer().addEventHandler((SequentialSchedulerContainerEventHandler) sequentialScheduler -> {
            addSS.accept(sequentialScheduler);

            sequentialScheduler.getEventHandlerContainer().addEventHandler(new SequentialSchedulerContainerEventHandler() {
                @Override
                public void addedSequentialScheduler(SequentialScheduler sequentialScheduler2) {
                    addSS.accept(sequentialScheduler2);

                    sequentialScheduler2.getEventHandlerContainer().addEventHandler(this);
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
