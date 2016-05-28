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
import java.util.function.Consumer;

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

                /*currentConstructor.bindTo(new Parameter() {
                    @Override
                    public void removeValue() {
                        contentPane.remove(currentConstructor);
                        contentPane.revalidate();
                        contentPane.repaint();

                        //valueConsumer.removeValue();
                    }

                    @Override
                    public void replaceValue(Value value) {
                        ((JComponent)value).setLocation(currentConstructor.getLocation());
                        contentPane.add(((JComponent)value));
                        contentPane.revalidate();
                        contentPane.repaint();

                        currentConstructor = null;

                        valueConsumer.replaceValue(value);
                    }
                });*/

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

                /*if(currentConstructor != null) {
                    currentConstructor.unbind();

                    contentPane.remove(currentConstructor);
                    contentPane.revalidate();
                    contentPane.repaint();
                }

                currentConstructor = new ConstructorCell(text -> {
                    return ComponentParser.parse(new Workspace() {
                        @Override
                        public void construct(Value target, Consumer<Value> valueConsumer) {
                            target.toString();
                        }
                    }, text);
                });

                currentConstructor.bindTo(new Parameter() {
                    @Override
                    public void removeValue() {
                        contentPane.remove(currentConstructor);
                        contentPane.revalidate();
                        contentPane.repaint();
                    }

                    @Override
                    public void replaceValue(Value value) {
                        ((JComponent)value).setLocation(currentConstructor.getLocation());
                        contentPane.add(((JComponent)value));
                        contentPane.revalidate();
                        contentPane.repaint();

                        currentConstructor = null;
                    }
                });

                currentConstructor.setLocation(e.getPoint());

                contentPane.add(currentConstructor);
                currentConstructor.revalidate();
                currentConstructor.repaint();
                currentConstructor.requestFocusInWindow();*/
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
