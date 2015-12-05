package midmod.editor;

import midmod.ListCell;
import midmod.MapCell;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();

        ProxyCellEditor editor = new ProxyCellEditor((current, replacement) -> {
            frame.getContentPane().remove(current);
            frame.getContentPane().add(replacement);
            frame.repaint();
            frame.revalidate();
        }) {
            @Override
            protected JComponent replaceWithComponent(String str) {
                return
                    str.equals("{") ? new MapCellEditor(new MapCell())
                        : str.equals("[") ? new ListCellEditor(new ListCell())
                        : null;
            }
        };

        frame.getContentPane().add(editor);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}
