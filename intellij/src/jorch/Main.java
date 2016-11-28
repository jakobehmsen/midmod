package jorch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
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
    }

    public static class Step1_2 implements Consumer<SequentialScheduler>, Serializable, Deprecated {
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

        @Override
        public <T> T upgrade() {
            return (T) new Step1_3();
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

    public static class Step2 implements Consumer<SequentialScheduler> {
        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            System.out.println("Step 2 started");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Step 2 ended");
            sequentialScheduler.finish(new Random().nextInt());
        }
    }

    public static class StepLast implements Consumer<SequentialScheduler> {
        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            sequentialScheduler.finish(-1);
        }
    }

    public static class Step3ForkAndMerge implements Consumer<SequentialScheduler>, Serializable {
        private static final long serialVersionUID = 7747282616039058003L;

        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            SequentialScheduler ss1 = sequentialScheduler.newSequentialScheduler(new Step1());
            SequentialScheduler ss2 = sequentialScheduler.newSequentialScheduler(new Step1());
            /*SequentialScheduler ss1 = sequentialScheduler.getSequentialSchedulers().size() > 0
                ? sequentialScheduler.getSequentialSchedulers().get(0) : sequentialScheduler.newSequentialScheduler(new Step1());
            SequentialScheduler ss2 = sequentialScheduler.getSequentialSchedulers().size() > 1
                ? sequentialScheduler.getSequentialSchedulers().get(1) : sequentialScheduler.newSequentialScheduler(new Step1());*/

            Future<Integer> s1 = executorService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    if(((SQLSequentialScheduler)ss1).hasMore()) {
                        CountDownLatch countDownLatch = new CountDownLatch(1);

                        ss1.getEventHandlerContainer().addEventHandler(new SequentialSchedulerEventHandler() {
                            @Override
                            public void proceeded() {

                            }

                            @Override
                            public void finished() {
                                countDownLatch.countDown();
                                ss1.getEventHandlerContainer().removeEventHandler(this);
                            }

                            @Override
                            public void wasClosed() {

                            }
                        });

                        countDownLatch.await();
                    }

                    return (Integer) ss1.getResult();
                }
            });
            Future<Integer> s2 = executorService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    if(((SQLSequentialScheduler)ss2).hasMore()) {
                        CountDownLatch countDownLatch = new CountDownLatch(1);

                        ss2.getEventHandlerContainer().addEventHandler(new SequentialSchedulerEventHandler() {
                            @Override
                            public void proceeded() {

                            }

                            @Override
                            public void finished() {
                                countDownLatch.countDown();
                                ss2.getEventHandlerContainer().removeEventHandler(this);
                            }

                            @Override
                            public void wasClosed() {

                            }
                        });

                        countDownLatch.await();
                    }

                    return (Integer) ss2.getResult();
                }
            });

            int s1Result = 0;
            try {
                s1Result = s1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            int s2Result = 0;
            try {
                s2Result = s2.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            System.out.println("Result from s1: " + s1Result);
            System.out.println("Result from s2: " + s2Result);

            System.out.println("Merged and all are finished");
            sequentialScheduler.finish(new Random().nextInt());
        }
    }

    public static void main(String[] args) throws Exception {
        SQLRepository repository = new SQLRepository(new LoadStrategy() {
            @Override
            public Consumer<SequentialScheduler> load(Consumer<SequentialScheduler> task) {
                while(true) {
                    if (task instanceof Deprecated)
                        task = ((Deprecated) task).upgrade();
                    else if (task instanceof Step1)
                        task = new Step1_2();
                    else
                        break;
                }

                return task;
            }
        });
        //SQLSequentialScheduler ss = repository.newSequentialScheduler();
        //SQLSequentialScheduler ss = repository.allSequentialSchedulers().get(0);

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
