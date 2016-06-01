package paidia;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AtomValue extends AbstractValue {
    private Workspace workspace;
    private String source;
    private Object number;
    //private Parameter parameter;

    public AtomValue(Workspace workspace, String source, Object number) {
        this.workspace = workspace;
        this.source = source;
        this.number = number;
        //setValue(number);

        //setSize(getPreferredSize());
    }

    @Override
    public ViewBinding toComponent() {
        JLabel view = new JLabel(source);

        view.setSize(view.getPreferredSize());

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    ConstructorCell constructorCell = new ConstructorCell(source, c -> ComponentParser.parse(workspace, c));
                    //parameter.replaceValue(constructorCell);
                    sendReplaceValue(constructorCell);
                }
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

        /*JSpinner view = new JSpinner();

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
        };*/
    }

    @Override
    public String toSource() {
        return source;
    }
}
