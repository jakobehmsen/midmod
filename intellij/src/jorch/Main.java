package jorch;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Todo: all kinds of steps should be serializable
        // Todo: token should serialize steps along its flow
        DefaultDependencyInjector dependencyInjector = new DefaultDependencyInjector();

        Step testProcess =
            new JavaStep(dependencyInjector, ContactTypeStep.class)
            .then(new JavaStep(dependencyInjector, LegitimationStep.class));

        /*CallStep callStep = new CallStep(callSiteContext -> {
            Hashtable<String, Object> callContext = new Hashtable<>();

            callContext.put("x", 100);

            return callContext;
        }, (callSiteContext, callContext) -> {
            callContext.put("result", callSiteContext.get("result"));
        }, new Step() {
            @Override
            public void perform(Token token, Map<String, Object> context) {
                int x = (int) context.get("x");

                int result = x * x;

                context.put("result", result);

                token.moveNext();
            }
        });

        Hashtable<String, Object> testCallContext = new Hashtable<>();
        DefaultToken.run(testCallContext, callStep);*/

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

        contentPane.add(tokens, BorderLayout.WEST);

        boolean[] halt = new boolean[1];

        dependencyInjector.put(SwingStepContext.class, new SwingStepContext() {
            @Override
            public JFrame getFrame() {
                return frame;
            }

            @Override
            public JComponent getComponent() {
                return stepComponent;
            }

            @Override
            public void halt() {
                halt[0] = true;


                //ObjectOutputStream objectOutputStream = new ObjectOutputStream();
            }

            @Override
            public void resume() {
                halt[0] = false;
            }
        });

        Hashtable<String, Object> context = new Hashtable<>();

        /*DefaultToken token = new DefaultToken(context, testProcess) {
            // stepComponent should not be serialized
            // stepComponent should be bound to the stepComponent made just above
            @Override
            public void perform(Map<String, Object> context, Step step, Step callback) {
                System.out.println("Performing " + step);
                super.perform(context, step, callback);
                System.out.println("Performed " + step);

                scheduleProceed();
            }

            @Override
            protected void performTask(Runnable taskPerformer) {
                stepComponent.removeAll();
                stepComponent.repaint();
                stepComponent.revalidate();
                super.performTask(taskPerformer);
            }

            @Override
            public void moveNext() {
                System.out.println("Moving next");
                super.moveNext();
                System.out.println("Moved next");
                halt[0] = false;
                scheduleProceed();
            }

            @Override
            protected void moveNextTask(Runnable moveNextPerformer) {
                super.moveNextTask(moveNextPerformer);

                stepComponent.removeAll();
                stepComponent.add(new JLabel("Awaiting..."));
                stepComponent.repaint();
                stepComponent.revalidate();
            }

            private void scheduleProceed() {
                if(halt[0])
                    return;

                new Thread(() -> proceed()).run();
            }

            @Override
            protected void finished() {
                stepComponent.removeAll();
                stepComponent.add(new JLabel("Finished it all."));
                stepComponent.repaint();
                stepComponent.revalidate();

                proceedButton.setEnabled(false);
            }
        };*/

        /*proceedButton.addActionListener(e ->
            token.proceed());*/

        newButton.addActionListener(e -> {
            DefaultToken token = new DefaultToken(context, testProcess) {
                // stepComponent should not be serialized
                // stepComponent should be bound to the stepComponent made just above
                @Override
                public void perform(Map<String, Object> context, Step step, Step callback) {
                    System.out.println("Performing " + step);
                    super.perform(context, step, callback);
                    System.out.println("Performed " + step);

                    scheduleProceed();
                }

                @Override
                protected void performTask(Runnable taskPerformer) {
                    stepComponent.removeAll();
                    stepComponent.repaint();
                    stepComponent.revalidate();
                    super.performTask(taskPerformer);
                }

                @Override
                public void moveNext() {
                    System.out.println("Moving next");
                    super.moveNext();
                    System.out.println("Moved next");
                    halt[0] = false;
                    scheduleProceed();

                    int indexOfToken = ((DefaultListModel<Token>)tokens.getModel()).indexOf(this);
                    ((DefaultListModel<Token>)tokens.getModel()).set(indexOfToken, this);
                }

                @Override
                protected void moveNextTask(Runnable moveNextPerformer) {
                    super.moveNextTask(moveNextPerformer);

                    stepComponent.removeAll();
                    stepComponent.add(new JLabel("Awaiting..."));
                    stepComponent.repaint();
                    stepComponent.revalidate();
                }

                private void scheduleProceed() {
                    if(halt[0])
                        return;

                    new Thread(() -> proceed()).run();
                }

                @Override
                protected void finished() {
                    ((DefaultListModel<Token>)tokens.getModel()).removeElement(this);

                    stepComponent.removeAll();
                    stepComponent.add(new JLabel("Finished it all."));
                    stepComponent.repaint();
                    stepComponent.revalidate();

                    proceedButton.setEnabled(false);
                }

                @Override
                public String toString() {
                    return "Token at " + currentStep();
                }
            };

            ((DefaultListModel<Token>)tokens.getModel()).addElement(token);
        });

        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
