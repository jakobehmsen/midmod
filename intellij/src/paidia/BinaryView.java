package paidia;

import javax.swing.*;

public class BinaryView extends JPanel {
    public void setup(PlaygroundView playgroundView) {
        JLabel valueViewLhs = new JLabel("lhs");
        EditableView lhsView = playgroundView.createEditableView(new Editor() {
            @Override
            public String getText() {
                return valueViewLhs.getText();
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                editorComponent.setPreferredSize(valueViewLhs.getPreferredSize());
                remove(0);
                add(editorComponent, 0);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void endEdit(String text) {
                valueViewLhs.setText(text);
                remove(0);
                add(valueViewLhs, 0);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void cancelEdit() {
                remove(0);
                add(valueViewLhs, 0);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }
        });
        add(valueViewLhs, 0);
        playgroundView.makeEditableByMouse(lhsView, valueViewLhs);

        JLabel valueViewRhs = new JLabel("rhs");
        EditableView rhsView = playgroundView.createEditableView(new Editor() {
            @Override
            public String getText() {
                return valueViewLhs.getText();
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                editorComponent.setPreferredSize(valueViewRhs.getPreferredSize());
                remove(1);
                add(editorComponent, 1);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void endEdit(String text) {
                valueViewRhs.setText(text);
                remove(1);
                add(valueViewRhs, 1);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void cancelEdit() {
                remove(1);
                add(valueViewRhs, 1);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }
        });
        add(valueViewRhs, 1);
        playgroundView.makeEditableByMouse(rhsView, valueViewRhs);

        setBorder(BorderFactory.createRaisedSoftBevelBorder());

        setSize(getPreferredSize());
    }
}
