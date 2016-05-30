package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
            ViewBinding currentConstructor;

            private void constructDialog(Point location, Parameter valueConsumer) {
                if(currentConstructor != null) {
                    //currentConstructor.unbind();

                    contentPane.remove(currentConstructor.getView());
                    currentConstructor.release();
                    contentPane.revalidate();
                    contentPane.repaint();
                }

                ConstructorCell constructorCell = new ConstructorCell("", text -> {
                    return ComponentParser.parse(new Workspace() {
                        @Override
                        public void construct(Value target, Parameter valueConsumer) {
                            Point p = SwingUtilities.convertPoint((JComponent)target, ((JComponent)target).getLocation(), contentPane);
                            constructDialog(p, new Parameter() {
                                @Override
                                public void removeValue() {
                                    contentPane.remove(currentConstructor.getView());
                                    contentPane.revalidate();
                                    contentPane.repaint();

                                    currentConstructor = null;
                                }

                                @Override
                                public void replaceValue(Value value) {
                                    contentPane.remove(currentConstructor.getView());

                                    currentConstructor = null;

                                    valueConsumer.replaceValue(value);

                                    contentPane.revalidate();
                                    contentPane.repaint();
                                }
                            });
                            contentPane.setComponentZOrder(currentConstructor.getView(), 0);
                        }
                    }, text);
                });

                currentConstructor = constructorCell.toComponent();

                /*currentConstructor = new ConstructorCell(text -> {
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
                });*/

                constructorCell.bindTo(valueConsumer);

                currentConstructor.getView().setLocation(location);

                contentPane.add(currentConstructor.getView());
                currentConstructor.getView().revalidate();
                currentConstructor.getView().repaint();
                currentConstructor.getView().requestFocusInWindow();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                constructDialog(e.getPoint(), new Parameter() {
                    ViewBinding viewBinding;

                    private ViewBinding getViewBinding() {
                        if(viewBinding == null)
                            return currentConstructor;
                        return viewBinding;
                    }

                    @Override
                    public void removeValue() {
                        contentPane.remove(getViewBinding().getView());
                        contentPane.revalidate();
                        contentPane.repaint();

                        currentConstructor = null;
                    }

                    @Override
                    public void replaceValue(Value value) {
                        // Use prefered size; listen for size changes
                        contentPane.remove(getViewBinding().getView());
                        ViewBinding newViewBinding = value.toComponent();
                        JComponent valueAsComponent = newViewBinding.getView();

                        valueAsComponent.addComponentListener(new ComponentAdapter() {
                            @Override
                            public void componentResized(ComponentEvent e) {
                                e.getComponent().revalidate();
                                e.getComponent().repaint();
                            }
                        });

                        valueAsComponent.setLocation(getViewBinding().getView().getLocation());
                        contentPane.add(valueAsComponent);
                        contentPane.revalidate();
                        contentPane.repaint();

                        value.bindTo(this);

                        if(getViewBinding() == currentConstructor)
                            currentConstructor = null;

                        viewBinding = newViewBinding;
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
