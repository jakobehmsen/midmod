package jorch;

import javax.swing.*;
import java.util.Hashtable;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        ActivityModel testProcessModel = new QuotedActivityModel(new Step() {
            @Override
            public void perform(Token token, Map<String, Object> context) {
                System.out.println("First step yay!!!");
                token.moveNext();
            }

            @Override
            public String toString() {
                return "Step 1";
            }
        }).then(new QuotedActivityModel(new Step() {
            @Override
            public void perform(Token token, Map<String, Object> context) {
                System.out.println("Second step yay!!!");
                token.moveNext();
            }

            @Override
            public String toString() {
                return "Step 2";
            }
        })).then(new QuotedActivityModel(new Step() {
            @Override
            public void perform(Token token, Map<String, Object> context) {
                System.out.println("Third step yay!!!");
                token.moveNext();
            }

            @Override
            public String toString() {
                return "Step 3";
            }
        }));

        CallStep callStep = new CallStep(callSiteContext -> {
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
        DefaultToken.run(testCallContext, callStep);

        Step testProcess = testProcessModel.toStep();

        JFrame frame = new JFrame();
        JButton proceedButton = new JButton("Proceed");
        frame.getContentPane().add(proceedButton);

        Hashtable<String, Object> context = new Hashtable<>();

        DefaultToken token = new DefaultToken(context, testProcess, (t, ctx) -> {
            System.out.println("Finished it all!!!");

            proceedButton.setEnabled(false);
        }) {
            @Override
            public void perform(Map<String, Object> context, Step step, Step callback) {
                System.out.println("Performing " + step);
                super.perform(context, step, callback);
                System.out.println("Performed " + step);
            }

            @Override
            public void moveNext() {
                System.out.println("Moving next");
                super.moveNext();
                System.out.println("Moved next");
            }

            /*private void scheduleProceed() {
                new Thread(() -> proceed()).run();
            }*/
        };

        proceedButton.addActionListener(e -> token.proceed());

        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
