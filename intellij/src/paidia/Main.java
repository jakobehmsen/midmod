package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

            Workspace workspace = new Workspace() {
                Workspace self = this;

                @Override
                public void setupView(Supplier<Value> value, ViewBinding viewBinding, Supplier<String> sourceGetter, Consumer<Value> valueReplacer) {
                    JComponent view = viewBinding.getView();
                    MouseAdapter mouseAdapter1 = new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                                ConstructorCell constructorCell = new ConstructorCell(sourceGetter.get(), c -> ComponentParser.parse(self, c));
                                viewBinding.setupEditor(constructorCell);
                                valueReplacer.accept(constructorCell);
                            }
                        }

                        private JComponent selection;
                        private int mousePressX;
                        private int mousePressY;
                        private boolean linking;
                        private boolean moving;

                        @Override
                        public void mousePressed(MouseEvent e) {
                            if(e.getButton() == MouseEvent.BUTTON3) {
                                if(view.getParent() != contentPane)
                                    return;

                                linking = true;
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
                                if(view.getParent() != contentPane)
                                    return;

                                moving = true;
                                view.getParent().setComponentZOrder(view, 0);
                                int cursorType = Cursor.MOVE_CURSOR;
                                Component glassPane = ((RootPaneContainer)contentPane.getTopLevelAncestor()).getGlassPane();
                                glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                                glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                                mousePressX = e.getX();
                                mousePressY = e.getY();
                            }
                        }

                        @Override
                        public void mouseDragged(MouseEvent e) {
                            if(linking) {

                            } else if(moving) {
                                int deltaX = e.getX() - mousePressX;
                                int deltaY = e.getY() - mousePressY;

                                view.setLocation(view.getX() + deltaX, view.getY() + deltaY);
                            }
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if(e.getButton() == MouseEvent.BUTTON3 && linking) {
                                linking = false;
                                int cursorType = Cursor.DEFAULT_CURSOR;
                                Component glassPane = ((RootPaneContainer)contentPane.getTopLevelAncestor()).getGlassPane();
                                glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                                glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                                contentPane.remove(selection);
                                contentPane.repaint(selection.getBounds());
                                contentPane.revalidate();

                                Point pointInContentPane = SwingUtilities.convertPoint(view, e.getPoint(), contentPane);
                                JComponent targetComponent = (JComponent) contentPane.findComponentAt(pointInContentPane);
                                Point pointInTargetComponent = SwingUtilities.convertPoint(contentPane, pointInContentPane, targetComponent);
                                if(targetComponent != view) {
                                    // Target must support dumping a value on it
                                    Value projection = new Reduction(value.get());

                                    //Value projection = value.get().createProjection();
                                    ViewBinding projectionView = projection.toComponent();
                                    projectionView.setupWorkspace(workspace);
                                    projectionView.getView().setLocation(pointInTargetComponent);
                                    targetComponent.add(projectionView.getView());

                                    projection.addUsage(new Usage() {
                                        ViewBinding origProjectionView = projectionView;

                                        @Override
                                        public void removeValue() {

                                        }

                                        @Override
                                        public void replaceValue(Value value) {
                                            if(origProjectionView.isCompatibleWith(value)) {
                                                origProjectionView.updateFrom(value);
                                                return;
                                            }

                                            ViewBinding projectionView = value.toComponent();
                                            projectionView.setupWorkspace(workspace);
                                            Point location = origProjectionView.getView().getLocation();
                                            targetComponent.remove(origProjectionView.getView());
                                            projectionView.getView().setLocation(location);
                                            targetComponent.add(projectionView.getView());
                                            origProjectionView = projectionView;
                                        }
                                    });
                                }

                            } else if(e.getButton() == MouseEvent.BUTTON1 && moving) {
                                moving = false;
                                int cursorType = Cursor.DEFAULT_CURSOR;
                                Component glassPane = ((RootPaneContainer)contentPane.getTopLevelAncestor()).getGlassPane();
                                glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                                glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
                            }
                        }
                    };

                    view.addMouseListener(mouseAdapter1);
                    view.addMouseMotionListener(mouseAdapter1);
                };
            };

            private void constructDialog(Point location, Usage usage) {
                if(currentConstructor != null) {

                    contentPane.remove(currentConstructor.getView());
                    currentConstructor.release();
                    contentPane.revalidate();
                    contentPane.repaint();
                }

                ConstructorCell constructorCell = new ConstructorCell("", text -> {
                    return ComponentParser.parse(workspace, text);
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
                    Value currentValue;

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
                        if(currentValue == value)
                            return;

                        currentValue = value;

                        // Use prefered size; listen for size changes
                        contentPane.remove(getViewBinding().getView());
                        ViewBinding newViewBinding = value.toComponent();
                        newViewBinding.setupWorkspace(workspace);
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
