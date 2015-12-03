package midmod.editor;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        MapCellEditor editor = new MapCellEditor();

        JFrame frame = new JFrame();

        frame.getContentPane().add(editor);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}
