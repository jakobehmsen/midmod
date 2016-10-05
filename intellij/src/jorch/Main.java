package jorch;

import javax.swing.*;

/**
 * Created by jakob on 04-10-16.
 */
public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JButton proceedButton = new JButton("Proceed");
        frame.getContentPane().add(proceedButton);

        DefaultToken token = new DefaultToken() {
            @Override
            public void moveForward(Step step) {
                System.out.println("Move forward to " + step);
                super.moveForward(step);
                System.out.println("Moved forward to " + step);

                //scheduleProceed();
            }

            @Override
            public void moveInto(Step step, Step callSite) {
                System.out.println("Move into " + step);
                super.moveInto(step, callSite);
                System.out.println("Moved into " + step);

                //scheduleProceed();
            }

            @Override
            public void moveOut() {
                Step currentStep = getCurrentStep();
                System.out.println("Moving out from " + currentStep);
                super.moveOut();
                System.out.println("Moved out from " + currentStep);

                //scheduleProceed();
            }

            /*private void scheduleProceed() {
                new Thread(() -> proceed()).run();
            }*/
        };

        proceedButton.addActionListener(e -> token.proceed());

        ActivityModel testProcessModel = new QuotedActivityModel(new Step() {
            @Override
            public void perform(Token token) {
                System.out.println("First step yay!!!");
                token.moveOut();
            }

            @Override
            public String toString() {
                return "Step 1";
            }
        }).then(new QuotedActivityModel(new Step() {
            @Override
            public void perform(Token token) {
                System.out.println("Second step yay!!!");
                token.moveOut();
            }

            @Override
            public String toString() {
                return "Step 2";
            }
        })).then(new QuotedActivityModel(new Step() {
            @Override
            public void perform(Token token) {
                System.out.println("Third step yay!!!");
                token.moveOut();
            }

            @Override
            public String toString() {
                return "Step 3";
            }
        }));

        Step testProcess = testProcessModel.toStep();

        /*token.enterFrame(input -> {
            input.put("X", 100);
        }, testProcess, (t, output) -> {
            System.out.println("Finished it all!!!");

            proceedButton.setEnabled(false);
        });*/

        token.enterFrame(input -> {
            input.put("X", 100);
        }, output -> {
            output.toString();
        });
        token.moveInto(testProcess, new Step() {
            @Override
            public void perform(Token token) {
                System.out.println("Finished it all!!!");

                proceedButton.setEnabled(false);
                token.exitFrame();;
            }
        });

        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
