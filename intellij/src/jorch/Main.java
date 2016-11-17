package jorch;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class Main {
    public static class Step1 implements Task2<Integer>, Consumer<SequentialScheduler> {
        @Override
        public Integer perform(TaskScheduler scheduler) {
            System.out.println("Step 1");
            scheduler.schedule(new StepLast());
            return -1;
        }

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

    public static class Step2 implements Task2<Integer>, Consumer<SequentialScheduler> {
        @Override
        public Integer perform(TaskScheduler scheduler) {
            System.out.println("Step 2");
            return new Random().nextInt();
        }

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

    public static class StepLast implements Task2<Integer>, Consumer<SequentialScheduler> {
        @Override
        public Integer perform(TaskScheduler scheduler) {
            scheduler.merge();
            return -1;
        }

        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            sequentialScheduler.finish(-1);
        }
    }

    public static class Step3ForkAndMerge implements Task2<Integer>, Consumer<SequentialScheduler> {
        @Override
        public Integer perform(TaskScheduler scheduler) {
            TaskScheduler inner = scheduler.fork();

            TaskFuture<Integer> s1 = inner.schedule(new Step1());
            TaskFuture<Integer> s2 = inner.schedule(new Step2());

            int s1Result = s1.get();
            int s2Result = s2.get();

            System.out.println("Result from s1: " + s1Result);
            System.out.println("Result from s2: " + s2Result);

            System.out.println("Merged and all are finished");
            return new Random().nextInt();
        }

        @Override
        public void accept(SequentialScheduler sequentialScheduler) {
            ConcurrentScheduler inner = sequentialScheduler.split();

            TaskFuture<Integer> s1 = inner.call(new Step1());
            TaskFuture<Integer> s2 = inner.call(new Step2());

            int s1Result = s1.get();
            int s2Result = s2.get();

            System.out.println("Result from s1: " + s1Result);
            System.out.println("Result from s2: " + s2Result);

            System.out.println("Merged and all are finished");
            sequentialScheduler.finish(new Random().nextInt());
        }
    }

    private static java.util.List<Token> persistedTokens;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        DefaultSequentialScheduler ss = new DefaultSequentialScheduler();

        ss.scheduleNext(new Step3ForkAndMerge());
        ss.proceedToFinish();

        if(1 != 2)
            return;

        DefaultTaskScheduler s = new DefaultTaskScheduler();

        s.schedule(new Step3ForkAndMerge());

        s.start();

        s.waitTillStop();


        boolean[] finished = new boolean[1];
        boolean[] halted = new boolean[1];

        DefaultScheduler scheduler = new DefaultScheduler("token14.tk", new Task() {
            @Override
            public void perform(Scheduler scheduler) {
                System.out.println("Before halt 1");

                scheduler.halt(new Task() {
                    @Override
                    public void perform(Scheduler scheduler) {
                        System.out.println("After halt 1");

                        System.out.println("Before halt 2");

                        scheduler.halt(new Task() {
                            @Override
                            public void perform(Scheduler scheduler) {
                                System.out.println("After halt 2");

                                scheduler.finish();
                            }
                        });
                    }
                });
            }

        }) {
            @Override
            protected void halted() {
                halted[0] = true;
            }

            @Override
            protected void scheduleTask(Task task) {
                super.scheduleTask(task);
            }

            @Override
            public void finish() {
                finished[0] = true;
            }
        };

        while(!finished[0]) {
            if(halted[0]) {
                System.out.print("Halted - hit any key to continue...");
                System.in.read();
                scheduler.resume();
            }

            scheduler.proceed();
        }

        System.out.println("Finished");

        if(1 != 2)
            return;

        // Todo: all kinds of steps should be serializable
        // Todo: token should serialize steps along its flow
        DefaultDependencyInjector dependencyInjector = new DefaultDependencyInjector();
        DependencyInjectorContext.setInstance(dependencyInjector);

        ActivityModel testProcessModel = dependencyInjector.newActivity(ContactTypeStep.class)
            .then(dependencyInjector.newActivity(LegitimationStep.class));

        JFrame frame = new JFrame();
        JButton proceedButton = new JButton("Proceed");

        JButton newButton = new JButton("New");

        JToolBar toolBar = new JToolBar();

        toolBar.add(proceedButton);
        toolBar.add(newButton);

        JPanel contentPane = new JPanel(new BorderLayout());
        frame.setContentPane(contentPane);
        contentPane.add(toolBar, BorderLayout.NORTH);
        JPanel stepComponent = new JPanel();
        stepComponent.add(new JLabel("Awaiting..."));
        contentPane.add(stepComponent, BorderLayout.CENTER);

        dependencyInjector.put(SwingStepContext.class, new SwingStepContext() {
            @Override
            public JFrame getFrame() {
                return frame;
            }

            @Override
            public JComponent getComponent() {
                return stepComponent;
            }
        });

        File persistedTokensFile = new File("tokens2346414866466.tks");

        /*ArrayList<String> al=new ArrayList<String>();
        al.add("Hello");
        al.add("Hi");
        al.add("Howdy");

        try{
            FileOutputStream fos= new FileOutputStream(persistedTokensFile);
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(al);
            oos.close();
            fos.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        ArrayList<String> arraylist= new ArrayList<String>();
        try
        {
            FileInputStream fis = new FileInputStream(persistedTokensFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            arraylist = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
            return;
        }catch(ClassNotFoundException c){
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
        for(String tmp: arraylist){
            System.out.println(tmp);
        }*/

        if(!persistedTokensFile.exists()) {
            persistedTokens = new ArrayList<>();
            FileOutputStream fos = new FileOutputStream(persistedTokensFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(persistedTokens);
            oos.close();
            fos.close();

            /*FileInputStream fis = new FileInputStream(persistedTokensFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            persistedTokens2 = (ArrayList) ois.readObject();
            ois.close();
            fis.close();*/
        } else {

            FileInputStream fis = new FileInputStream(persistedTokensFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            persistedTokens = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
        }

        DefaultListModel<Token> tokensModel = new DefaultListModel<>();

        persistedTokens.forEach(t -> tokensModel.addElement(t));

        JList<Token> tokens = new JList<>(tokensModel);

        contentPane.add(new JScrollPane(tokens), BorderLayout.WEST);

        Hashtable<String, Object> context = new Hashtable<>();

        TokenStore tokenStore = new TokenStore() {
            @Override
            public Token newToken(Map<String, Object> context, Step start) {
                Token token = new DefaultToken(context, start) {
                    private boolean halt;
                    private DefaultTokenConsumer pending;

                    private JComponent getStepComponent() {
                        return ((SwingStepContext)DependencyInjectorContext.getInstance().getInstance(SwingStepContext.class)).getComponent();
                    }

                    private JList<Token> getTokens() {
                        //return ((SwingStepContext)DependencyInjectorContext.getInstance().getInstance(SwingStepContext.class)).getComponent();
                        return
                            (JList<Token>) ((JScrollPane) ((BorderLayout)((SwingStepContext)DependencyInjectorContext.getInstance().getInstance(SwingStepContext.class)).getFrame().getContentPane().getLayout()).getLayoutComponent(BorderLayout.WEST)).getViewport().getView();
                    }

                    @Override
                    public void moveNext() {
                        halt = false;
                        super.moveNext();

                        persistTokens();
                    }

                    @Override
                    public void halt() {
                        halt = true;

                            persistTokens();
                    }

                    @Override
                    protected void finished() {
                        persistedTokens.remove(this);

                        ((DefaultListModel<Token>) getTokens().getModel()).removeElement(this);

                        getStepComponent().removeAll();
                        getStepComponent().add(new JLabel("Finished it all."));
                        getStepComponent().repaint();
                        getStepComponent().revalidate();

                        persistTokens();
                    }

                    @Override
                    protected void schedule(DefaultTokenConsumer runnable) {
                        pending = runnable;

                        if(halt)
                            return;

                        new Thread(() -> {
                            getStepComponent().removeAll();
                            getStepComponent().repaint();
                            getStepComponent().revalidate();

                            runnable.accept(this);

                            int indexOfToken = ((DefaultListModel<Token>) getTokens().getModel()).indexOf(this);
                            if(indexOfToken != -1) // Might have finished
                                ((DefaultListModel<Token>) getTokens().getModel()).set(indexOfToken, this);
                        }).run();
                    }

                    @Override
                    public String toString() {
                        return "Token at " + currentStep();
                    }

                    @Override
                    public void proceed() {
                        halt = false;
                        schedule(pending);

                        persistTokens();
                    }

                    private void writeObject(ObjectOutputStream oos)
                        throws IOException {
                        oos.writeBoolean(halt);
                        oos.writeObject(pending);
                    }

                    private void readObject(ObjectInputStream ois)
                        throws ClassNotFoundException, IOException {
                        halt = ois.readBoolean();
                        pending = (DefaultTokenConsumer) ois.readObject();
                    }
                };

                persistedTokens.add(token);

                persistTokens();

                return token;
            }

            @Override
            public void removeToken(Token token) {
                persistedTokens.remove(token);

                persistTokens();
            }

            @Override
            public List<Token> getTokens() {
                return persistedTokens;
            }

            private void persistTokens() {
                try {
                    FileOutputStream fos = new FileOutputStream(persistedTokensFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(persistedTokens);
                    oos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        proceedButton.addActionListener(e -> {
            if(tokens.getSelectedIndex() != -1)
                tokens.getSelectedValue().proceed();
        });

        newButton.addActionListener(e -> {
            Step testProcess = testProcessModel.toStep();
            Token token = tokenStore.newToken(context, testProcess);

            ((DefaultListModel<Token>)tokens.getModel()).addElement(token);
        });

        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
