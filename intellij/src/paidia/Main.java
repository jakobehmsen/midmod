package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Paidia");

        frame.setContentPane(new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        });
        JComponent contentPane = (JComponent) frame.getContentPane();

        contentPane.setLayout(null);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            ConstructorCell currentConstructor;

            private void constructDialog(Point location, Parameter valueConsumer) {
                if(currentConstructor != null) {
                    //currentConstructor.unbind();

                    contentPane.remove(currentConstructor);
                    contentPane.revalidate();
                    contentPane.repaint();
                }

                currentConstructor = new ConstructorCell(text -> {
                    return ComponentParser.parse(new Workspace() {
                        @Override
                        public void construct(Value target, Parameter valueConsumer) {
                            Point p = SwingUtilities.convertPoint((JComponent)target, ((JComponent)target).getLocation(), contentPane);
                            constructDialog(p, new Parameter() {
                                @Override
                                public void removeValue() {
                                    contentPane.remove(currentConstructor);
                                    contentPane.revalidate();
                                    contentPane.repaint();

                                    currentConstructor = null;
                                }

                                @Override
                                public void replaceValue(Value value) {
                                    contentPane.remove(currentConstructor);

                                    currentConstructor = null;

                                    valueConsumer.replaceValue(value);

                                    contentPane.revalidate();
                                    contentPane.repaint();
                                }
                            });
                            contentPane.setComponentZOrder(currentConstructor, 0);
                        }
                    }, text);
                });

                currentConstructor.bindTo(valueConsumer);

                currentConstructor.setLocation(location);

                contentPane.add(currentConstructor);
                currentConstructor.revalidate();
                currentConstructor.repaint();
                currentConstructor.requestFocusInWindow();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                constructDialog(e.getPoint(), new Parameter() {
                    @Override
                    public void removeValue() {
                        contentPane.remove(currentConstructor);
                        contentPane.revalidate();
                        contentPane.repaint();

                        currentConstructor = null;
                    }

                    @Override
                    public void replaceValue(Value value) {
                        contentPane.remove(currentConstructor);
                        ((JComponent)value).setLocation(currentConstructor.getLocation());
                        contentPane.add(((JComponent)value));
                        contentPane.revalidate();
                        contentPane.repaint();

                        currentConstructor = null;
                    }
                });
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
