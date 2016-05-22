package paidia;

import com.sun.glass.events.KeyEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Paidia");

        JComponent contentPane = (JComponent) frame.getContentPane();

        contentPane.setLayout(null);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            JComponent currentConstructor;

            @Override
            public void mouseClicked(MouseEvent e) {
                if(currentConstructor != null) {
                    contentPane.remove(currentConstructor);
                    contentPane.revalidate();
                    contentPane.repaint();
                }

                JPanel panel = new JPanel(new BorderLayout());
                JTextArea constructor = new JTextArea();

                panel.add(constructor, BorderLayout.CENTER);

                int height = constructor.getFontMetrics(constructor.getFont()).getHeight();
                panel.setLocation(e.getPoint());
                panel.setSize(200, height);

                constructor.registerKeyboardAction(e1 -> {
                    String text = constructor.getText();

                    JComponent view = ComponentParser.parse(text);

                    contentPane.remove(panel);
                    view.setLocation(panel.getLocation());
                    contentPane.add(view);
                    contentPane.revalidate();
                    contentPane.repaint();

                    currentConstructor = null;
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

                constructor.registerKeyboardAction(e1 -> {
                    contentPane.remove(panel);
                    contentPane.revalidate();
                    contentPane.repaint();

                    currentConstructor = null;
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

                constructor.registerKeyboardAction(e1 -> {
                    try {
                        constructor.getDocument().insertString(constructor.getDocument().getLength(), "\n", null);
                    } catch (BadLocationException e2) {
                        e2.printStackTrace();
                    }
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.MODIFIER_ALT), JComponent.WHEN_FOCUSED);

                constructor.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        try {
                            String text = e.getDocument().getText(e.getOffset(), e.getLength());
                            if(text.matches("\r\n|\r|\n")) {
                                int additionalHeight = text.equals("\n") ? 1 : text.split("\r\n|\r|\n").length;
                                panel.setSize(200, panel.getHeight() + additionalHeight * height);
                            }
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        try {
                            String text = e.getDocument().getText(e.getOffset(), e.getLength());
                            if(text.matches("\r\n|\r|\n")) {
                                int additionalHeight = text.equals("\n") ? 1 : text.split("\r\n|\r|\n").length;
                                panel.setSize(200, panel.getHeight() - additionalHeight * height);
                            }
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {

                    }
                });

                contentPane.add(panel);
                panel.revalidate();
                panel.repaint();
                panel.requestFocusInWindow();

                currentConstructor = panel;
            }
        };
        contentPane.addMouseListener(mouseAdapter);
        contentPane.addMouseMotionListener(mouseAdapter);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
