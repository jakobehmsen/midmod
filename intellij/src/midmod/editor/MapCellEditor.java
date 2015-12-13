package midmod.editor;

import midmod.ListCell;
import midmod.MapCell;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class MapCellEditor extends JPanel {
    private MapCell mapCell;
    private JPanel entries = new JPanel();
    private JPanel names = new JPanel();
    private JPanel values = new JPanel();

    public MapCellEditor(MapCell mapCell) {
        this.mapCell = mapCell;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JLabel l1 = new JLabel("{");
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        l1.setAlignmentY(Component.TOP_ALIGNMENT);
        add(l1);

        entries.setLayout(new BoxLayout(entries, BoxLayout.X_AXIS));
        entries.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        entries.setAlignmentX(Component.LEFT_ALIGNMENT);
        entries.setAlignmentY(Component.TOP_ALIGNMENT);
        names.setLayout(new BoxLayout(names, BoxLayout.PAGE_AXIS));
        //entries.add(names);
        values.setLayout(new BoxLayout(values, BoxLayout.PAGE_AXIS));
        //entries.add(values);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, names, values);
        splitPane.setResizeWeight(0.25);
        //entries.add(splitPane);

        //entries.setLayout(new GridLayout(0, 2));
        /*entries.setLayout(new TableLayout());
        ((TableLayout)entries.getLayout()).setColumn(new double[]{0.25, 0.75});*/

        add(entries);

        JLabel l2 = new JLabel("}");
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);
        l2.setAlignmentY(Component.TOP_ALIGNMENT);
        add(l2);

        newEntry();
    }

    private void newEntry() {
        JTextField editor = new JTextField() {
            {
                JTextField self = this;

                StyledDocument document = new DefaultStyledDocument() {
                    @Override
                    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                        if(str.equals(":")) {
                            String name = self.getText();
                            newEntry(name);
                            setText("");
                            MapCellEditor.this.revalidate();
                            MapCellEditor.this.repaint();
                        } else {
                            super.insertString(offs, str, a);
                            setSize(getPreferredSize());
                        }
                    }

                    @Override
                    public void remove(int offs, int len) throws BadLocationException {
                        super.remove(offs, len);
                        setSize(getPreferredSize());
                    }
                };

                setDocument(document);
            }

            @Override
            public void addNotify() {
                super.addNotify();
                requestFocus();
            }
        };

        /*ProxyCellEditor editor = new ProxyCellEditor((current, replacement) -> {
            replacement.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            replacement.setAlignmentX(Component.LEFT_ALIGNMENT);
            replacement.setAlignmentY(Component.TOP_ALIGNMENT);

            replacement.setAlignmentX(Component.LEFT_ALIGNMENT);
            replacement.setAlignmentY(Component.TOP_ALIGNMENT);

            int zOrder = getComponentZOrder(current);
            remove(current);
            add(replacement);
            setComponentZOrder(replacement, zOrder);

            newEntry();

            revalidate();
            repaint();
        }) {
            @Override
            protected JComponent replaceWithComponent(String str) {
                if(str.equals(":"))
                    return new MapEntryEditor(getText().substring(0, getText().length()));
                return null;
            }
        };*/

        editor.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        editor.setAlignmentX(Component.LEFT_ALIGNMENT);
        editor.setAlignmentY(Component.TOP_ALIGNMENT);

        add(editor, getComponentCount() - 1);
    }

    private void newEntry(String name) {
        JPanel nameView = new JPanel();

        nameView.setLayout(new BoxLayout(nameView, BoxLayout.X_AXIS));

        nameView.add(new JTextField(name) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 20);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(super.getMaximumSize().width, 20);
            }
        });
        nameView.add(new JLabel(":"));

        //names.add(nameView);

        ProxyCellEditor valueEditor = new ProxyCellEditor((current, replacement) -> {
            int zIndex = entries.getComponentZOrder(current);
            entries.remove(current);
            entries.add(replacement, zIndex);
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

        /*valueEditor.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                nameView.setPreferredSize(new Dimension(nameView.getPreferredSize().width, valueEditor.getPreferredSize().height));
            }
        });

        values.add(valueEditor);*/

        /*((TableLayout)entries.getLayout()).insertRow(entries.getComponentCount(), 1.0);
        TableLayoutConstraints c = new TableLayoutConstraints();
        c.row1 = entries.getComponentCount();
        c.row2 = entries.getComponentCount();
        entries.add(nameView, c);
        entries.getComponent(entries.getComponentCount() - 1).setPreferredSize(new Dimension(20, 20));
        c.col1 = 1;
        c.col2 = 1;
        c.row1 = entries.getComponentCount();
        c.row2 = entries.getComponentCount();
        entries.add(valueEditor, c);*/
    }
}
