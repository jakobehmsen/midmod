package paidia;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ScopeView extends CompositeValueView {
    private JLabel nameLabel;

    public ScopeView(ValueView valueView) {
        setLayout(new BorderLayout());

        addChild(valueView);

        setSize(getPreferredSize());

        //setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));

        setBorder(new RoundedBorder());
    }

    @Override
    protected void addChildAsComponent(int index, JComponent child) {
        add(child, BorderLayout.CENTER);
    }

    @Override
    protected void removeChildAsComponent(int index, JComponent child) {
        JComponent theValueView = (JComponent) ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER);
        remove(theValueView);
    }

    @Override
    public String getSource(TextContext textContext) {
        return getChild(0).getSource(textContext);
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        return getChild(0).evaluate(environment);
    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        JComponent theValueView = (JComponent) ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER);
        JComponent parent = this;//(JComponent) valueView.getParent();

        return new ChildSlot() {
            JComponent current = theValueView;

            @Override
            public void replace(JComponent view) {
                parent.remove(current);
                parent.add(view, BorderLayout.CENTER);
                current = view;
            }

            @Override
            public void revert() {
                replace(theValueView);
            }

            @Override
            public void commit(JComponent valueView) {
                replace(valueView);
            }
        };
    }

    @Override
    public String getSummaryText() {
        return nameLabel != null ? nameLabel.getText() : getChild(0).getText();
    }

    public void beginEditName() {
        boolean nameWasEmpty = nameLabel == null;

        getPlaygroundView().createEditableView(new Editor() {
            JComponent theEditorComponent;

            @Override
            public String getText() {
                return nameLabel != null ? nameLabel.getText() : "";
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                if(!nameWasEmpty)
                    remove(nameLabel);

                add(editorComponent, BorderLayout.NORTH);

                theEditorComponent = editorComponent;
            }

            @Override
            public void endEdit(String text) {
                remove(theEditorComponent);

                if(nameWasEmpty) {
                    nameLabel = new JLabel();
                    nameLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
                    nameLabel.setOpaque(true);
                }

                if(text.equals("")) {
                    nameLabel = null;
                } else {
                    add(nameLabel, BorderLayout.NORTH);
                    nameLabel.setText(text);
                }

                sendUpdated();
            }

            @Override
            public void cancelEdit() {
                remove(theEditorComponent);
                if(!nameWasEmpty)
                    add(nameLabel, BorderLayout.NORTH);
            }
        }).beginEdit();
    }

    @Override
    public ValueView passAsReference() {
        return getChild(0).passAsReference();
    }
}
