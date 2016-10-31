package jorch;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main {
    private static java.util.List<Token> persistedTokens;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
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
