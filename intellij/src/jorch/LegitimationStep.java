package jorch;

import javax.swing.*;
import java.util.Map;

public class LegitimationStep implements Step {
    private SwingStepContext swingStepContext;

    public LegitimationStep(SwingStepContext swingStepContext) {
        this.swingStepContext = swingStepContext;
    }

    @Override
    public void perform(Token token, Map<String, Object> context) {
        swingStepContext.halt();

        JButton continueButton = new JButton("Continue");

        JComboBox<String> legitimationValidations = new JComboBox<>();

        continueButton.addActionListener(e -> {
            String legitimationValidation = (String) legitimationValidations.getSelectedItem();

            context.put("legitimationValidation", legitimationValidation);

            token.moveNext();
        });

        ((DefaultComboBoxModel<String>)legitimationValidations.getModel()).addElement("Accepted");
        ((DefaultComboBoxModel<String>)legitimationValidations.getModel()).addElement("Rejected");

        swingStepContext.getComponent().add(new JLabel("Legitimation"));
        swingStepContext.getComponent().add(legitimationValidations);
        swingStepContext.getComponent().add(continueButton);
    }

    @Override
    public String toString() {
        return "Legitimation";
    }
}
