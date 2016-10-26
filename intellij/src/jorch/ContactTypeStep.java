package jorch;

import javax.swing.*;

public class ContactTypeStep implements Step {
    private SwingStepContext swingStepContext;

    public ContactTypeStep(SwingStepContext swingStepContext) {
        this.swingStepContext = swingStepContext;
    }

    @Override
    public void perform(Token token) {
        token.halt();

        JButton continueButton = new JButton("Continue");

        JComboBox<String> contactForms = new JComboBox<>();

        continueButton.addActionListener(e -> {
            String contactForm = (String) contactForms.getSelectedItem();

            token.put("contactForm", contactForm);

            token.moveNext();
        });

        ((DefaultComboBoxModel<String>)contactForms.getModel()).addElement("Phone");
        ((DefaultComboBoxModel<String>)contactForms.getModel()).addElement("Mail");

        swingStepContext.getComponent().add(new JLabel("ContactForm"));
        swingStepContext.getComponent().add(contactForms);
        swingStepContext.getComponent().add(continueButton);
    }

    @Override
    public String toString() {
        return "Contact Type";
    }
}
