package midmod.editor;

import midmod.ListCell;
import midmod.MapCell;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class MapEntryEditor extends JPanel {
    private JTextField nameEditor;
    private JComponent valueEditor;

    public MapEntryEditor(String name) {
        nameEditor = new JTextField(name) {
            {
                StyledDocument document = new DefaultStyledDocument() {
                    @Override
                    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                        super.insertString(offs, str, a);
                        setSize(getPreferredSize());
                        revalidate();
                        repaint();
                    }

                    @Override
                    public void remove(int offs, int len) throws BadLocationException {
                        super.remove(offs, len);
                        setSize(getPreferredSize());
                        revalidate();
                        repaint();
                    }
                };

                setDocument(document);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 20);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(super.getPreferredSize().width, 20);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(super.getPreferredSize().width, 20);
            }
        };

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(nameEditor);
        add(new JLabel(":"));

        valueEditor = new ProxyCellEditor((current, replacement) -> {
            remove(2);
            add(replacement, 2);
            repaint();
            revalidate();
        }) {
            @Override
            protected JComponent replaceWithComponent(String str) {
                return
                    str.equals("{") ? new MapCellEditor(new MapCell())
                        : str.equals("[") ? new ListCellEditor(new ListCell())
                        : null;
            }
        };
        add(valueEditor);
    }
}
