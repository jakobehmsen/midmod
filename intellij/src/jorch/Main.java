package jorch;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Todo: all kinds of steps should be serializable
        // Todo: token should serialize steps along its flow
        DefaultDependencyInjector dependencyInjector = new DefaultDependencyInjector();

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

        JList<Token> tokens = new JList<>(new DefaultListModel<>());

        contentPane.add(new JScrollPane(tokens), BorderLayout.WEST);

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

        Hashtable<String, Object> context = new Hashtable<>();

        TokenFactory tokenFactory = new TokenFactory() {
            @Override
            public Token newToken(Map<String, Object> context, Step start) {
                return new DefaultToken(context, start) {
                    private boolean halt;
                    private Runnable pending;

                    @Override
                    public void moveNext() {
                        halt = false;
                        super.moveNext();
                    }

                    @Override
                    public void halt() {
                        halt = true;
                    }

                    @Override
                    protected void finished() {
                        ((DefaultListModel<Token>) tokens.getModel()).removeElement(this);

                        stepComponent.removeAll();
                        stepComponent.add(new JLabel("Finished it all."));
                        stepComponent.repaint();
                        stepComponent.revalidate();
                    }

                    @Override
                    protected void schedule(Runnable runnable) {
                        pending = runnable;

                        if(halt)
                            return;

                        new Thread(() -> {
                            stepComponent.removeAll();
                            stepComponent.repaint();
                            stepComponent.revalidate();

                            runnable.run();

                            int indexOfToken = ((DefaultListModel<Token>) tokens.getModel()).indexOf(this);
                            if(indexOfToken != -1) // Might have finished
                                ((DefaultListModel<Token>) tokens.getModel()).set(indexOfToken, this);
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
                };
            }
        };

        proceedButton.addActionListener(e -> {
            if(tokens.getSelectedIndex() != -1)
                tokens.getSelectedValue().proceed();
        });

        newButton.addActionListener(e -> {
            Step testProcess = testProcessModel.toStep();
            Token token = tokenFactory.newToken(context, testProcess);

            ((DefaultListModel<Token>)tokens.getModel()).addElement(token);
        });

        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
