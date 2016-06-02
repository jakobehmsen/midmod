package paidia;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

            private void constructDialog(Point location, Usage usage) {
                if(currentConstructor != null) {

                    contentPane.remove(currentConstructor.getView());
                    currentConstructor.release();
                    contentPane.revalidate();
                    contentPane.repaint();
                }

                ConstructorCell constructorCell = new ConstructorCell("", text -> {
                    return ComponentParser.parse(new Workspace() {
                        Workspace self = this;
                        ArrayList<JComponent> sdgf;

                        public void destroyView(JComponent view) {

                        }

                        @Override
                        public void setupView(JComponent view, Supplier<String> sourceGetter, Consumer<Value> valueReplacer) {
                            MouseAdapter mouseAdapter1 = new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                                        ConstructorCell constructorCell = new ConstructorCell(sourceGetter.get(), c -> ComponentParser.parse(self, c));
                                        valueReplacer.accept(constructorCell);
                                    }
                                }

                                private JComponent selection;
                                private int mousePressX;
                                private int mousePressY;
                                private int moveLastX;
                                private int moveLastY;
                                private boolean moving;

                                @Override
                                public void mousePressed(MouseEvent e) {
                                    if(e.getButton() == MouseEvent.BUTTON3) {
                                        int cursorType = Cursor.HAND_CURSOR;
                                        Component glassPane = ((RootPaneContainer)contentPane.getTopLevelAncestor()).getGlassPane();
                                        glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                                        glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                                        selection = new JPanel();
                                        selection.setBorder(BorderFactory.createDashedBorder(Color.BLACK));
                                        Point point = SwingUtilities.convertPoint(view.getParent(), view.getLocation(), contentPane);
                                        selection.setSize(view.getSize());
                                        selection.setLocation(point);
                                        selection.setOpaque(false);
                                        contentPane.add(selection, 0);
                                        selection.repaint();
                                        selection.revalidate();
                                    } else if(e.getButton() == MouseEvent.BUTTON1) {
                                        moving = true;
                                        int cursorType = Cursor.MOVE_CURSOR;
                                        Component glassPane = ((RootPaneContainer)contentPane.getTopLevelAncestor()).getGlassPane();
                                        glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                                        glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                                        moveLastX = e.getX();
                                        moveLastY = e.getY();

                                        mousePressX = e.getX();
                                        mousePressY = e.getY();
                                    }
                                }

                                @Override
                                public void mouseDragged(MouseEvent e) {
                                    if(moving) {

                                        int deltaX = e.getX() - mousePressX;
                                        int deltaY = e.getY() - mousePressY;

                                        System.out.println("e.getX()=" + e.getX());
                                        System.out.println("e.getY()=" + e.getY());

                                        System.out.println("deltaX=" + deltaX);
                                        System.out.println("deltaY=" + deltaY);

                                        moveLastX = e.getX();
                                        moveLastY = e.getY();

                                        view.setLocation(view.getX() + deltaX, view.getY() + deltaY);
                                        //view.repaint();
                                        //view.revalidate();
                                    }
                                }

                                @Override
                                public void mouseReleased(MouseEvent e) {
                                    if(e.getButton() == MouseEvent.BUTTON3) {
                                        int cursorType = Cursor.DEFAULT_CURSOR;
                                        Component glassPane = ((RootPaneContainer)contentPane.getTopLevelAncestor()).getGlassPane();
                                        glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                                        glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                                        contentPane.remove(selection);
                                        contentPane.repaint(selection.getBounds());
                                        contentPane.revalidate();
                                    } else if(e.getButton() == MouseEvent.BUTTON1) {
                                        moving = false;
                                        int cursorType = Cursor.DEFAULT_CURSOR;
                                        Component glassPane = ((RootPaneContainer)contentPane.getTopLevelAncestor()).getGlassPane();
                                        glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                                        glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                                        int deltaX = e.getX() - mousePressX;
                                        int deltaY = e.getY() - mousePressY;

                                        moveLastX = e.getX();
                                        moveLastY = e.getY();

                                        view.setLocation(view.getX() + deltaX, view.getY() + deltaY);
                                        //view.repaint();
                                        //view.revalidate();
                                    }
                                }
                            };

                            view.addMouseListener(mouseAdapter1);
                            view.addMouseMotionListener(mouseAdapter1);
                        }
                    }, text);
                });

                currentConstructor = constructorCell.toComponent();

                constructorCell.addUsage(usage);

                currentConstructor.getView().setLocation(location);

                contentPane.add(currentConstructor.getView());
                currentConstructor.getView().revalidate();
                currentConstructor.getView().repaint();
                currentConstructor.getView().requestFocusInWindow();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                constructDialog(e.getPoint(), new Usage() {
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

                        value.addUsage(this);

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
