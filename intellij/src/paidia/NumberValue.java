package paidia;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NumberValue implements Value {
    private Number number;

    public NumberValue(Number number) {
        this.number = number;
        //setValue(number);

        //setSize(getPreferredSize());
    }

    @Override
    public void bindTo(Parameter parameter) {

    }

    @Override
    public void unbind() {

    }

    @Override
    public ViewBinding toComponent() {
        JSpinner view = new JSpinner();

        JFormattedTextField field = ((JSpinner.DefaultEditor)view.getEditor()).getTextField();

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                view.setSize(view.getPreferredSize());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                view.setSize(view.getPreferredSize());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                view.setSize(view.getPreferredSize());
            }
        });

        view.setValue(number);
        view.setSize(view.getPreferredSize());

        view.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                view.setSize(view.getPreferredSize());
            }
        });

        view.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(evt.getPropertyName());
            }
        });

        return new ViewBinding() {
            @Override
            public JComponent getView() {
                return view;
            }

            @Override
            public void release() {

            }
        };
    }
}
