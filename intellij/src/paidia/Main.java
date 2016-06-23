package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        JFrame f = new JFrame("Paidia");

        JPanel contentPane2 = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        contentPane2.add(toolBar, BorderLayout.NORTH);
        contentPane2.add(new Playground(new MouseToolProvider() {
            MouseTool selectedMouseTool;
            JComponent canvas;

            @Override
            public void setCanvas(JComponent canvas) {
                this.canvas = canvas;

                addMouseTool("Move", new MouseTool() {
                    private JComponent targetValueView;
                    private int mousePressX;
                    private int mousePressY;
                    private boolean moving;

                    @Override
                    public void mousePressed(MouseEvent e) {
                        JComponent valueView = (JComponent) e.getComponent();
                        if(e.getButton() == MouseEvent.BUTTON1) {
                            targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() instanceof Playground).findFirst().get();

                            moving = true;
                            targetValueView.getParent().setComponentZOrder(targetValueView, 0);

                            int cursorType = Cursor.MOVE_CURSOR;
                            Component glassPane = f.getGlassPane();
                            glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                            glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                            mousePressX = e.getX();
                            mousePressY = e.getY();
                        }
                    }

                    @Override
                    public void mouseDragged(MouseEvent e) {
                        if(moving) {
                            canvas.setToolTipText("");

                            int deltaX = e.getX() - mousePressX;
                            int deltaY = e.getY() - mousePressY;

                            targetValueView.setLocation(targetValueView.getX() + deltaX, targetValueView.getY() + deltaY);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if(e.getButton() == MouseEvent.BUTTON1 && moving) {
                            moving = false;
                            int cursorType = Cursor.DEFAULT_CURSOR;
                            Component glassPane = f.getGlassPane();
                            glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                            glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
                        }
                    }

                    @Override
                    public void startTool(JComponent component) {
                        component.setToolTipText("Press and drag an object to move it.");
                    }
                });

                ((JButton)toolBar.getComponents()[0]).doClick();
            }

            private void addMouseTool(String text, MouseTool mouseTool) {
                toolBar.add(new AbstractAction(text) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(selectedMouseTool != null)
                            selectedMouseTool.endTool(canvas);
                        selectedMouseTool = mouseTool;
                        selectedMouseTool.startTool(canvas);

                        String title = f.getTitle().split(" - ")[0];
                        f.setTitle(title + " - " + text);
                    }
                });
            }

            @Override
            public MouseTool getMouseTool() {
                return selectedMouseTool;
            }
        }), BorderLayout.CENTER);

        /*PlaygroundView playgroundView = new PlaygroundView();

        f.setContentPane(playgroundView);*/

        f.setContentPane(contentPane2);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1024, 768);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        if(1 != 2)
            return;


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

            private ArrayList<ViewBinding> views = new ArrayList<>();

            private void addView(ViewBinding viewBinding) {
                contentPane.add(viewBinding.getView());
                views.add(viewBinding);
            }

            private void removeView(ViewBinding viewBinding) {
                contentPane.remove(viewBinding.getView());
                views.remove(viewBinding);
            }

            private ViewBinding findView(Point location) {
                Optional<ViewBinding> foundView = views.stream()
                    .sorted((x, y) -> x.getView().getParent().getComponentZOrder(x.getView()) - y.getView().getParent().getComponentZOrder(y.getView()))
                    .filter(x -> x.getView().getBounds().contains(location))
                    .findFirst();

                if(foundView.isPresent()) {
                    location.translate(-foundView.get().getView().getX(), -foundView.get().getView().getY());
                    return foundView.get().findView(location);
                } else {
                    return new ViewBinding() {
                        @Override
                        public JComponent getView() {
                            return contentPane;
                        }

                        @Override
                        public void release() {

                        }

                        @Override
                        public boolean isCompatibleWith(Value value) {
                            return false;
                        }

                        @Override
                        public void updateFrom(Value value) {

                        }

                        @Override
                        public boolean canDrop() {
                            return true;
                        }

                        @Override
                        public void drop(Value value, Point location) {
                            Usage usage = createUsage(false, location);
                            usage.replaceValue(value);
                        }
                    };
                }
            }

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
                                ViewBinding targetView = findView(pointInContentPane);
                                JComponent targetComponent = targetView.getView();//(JComponent) contentPane.findComponentAt(pointInContentPane);
                                Point pointInTargetComponent = SwingUtilities.convertPoint(contentPane, pointInContentPane, targetComponent);
                                if(targetComponent != view) {
                                    // Target must support dumping a value on it
                                    Value projection = new Reduction(value.get());

                                    //Value projection = value.get().createProjection();
                                    ViewBinding projectionView = projection.toComponent();
                                    projectionView.setupWorkspace(workspace);
                                    projectionView.getView().setLocation(pointInTargetComponent);
                                    targetView.drop(projection, pointInTargetComponent);
                                    //targetComponent.add(projectionView.getView());

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

                                            origProjectionView.drop(value, location);

                                            //targetComponent.remove(origProjectionView.getView());
                                            projectionView.getView().setLocation(location);
                                            //targetComponent.add(projectionView.getView());
                                            //targetView.drop(projection);
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

                @Override
                public void setupView(Supplier<Value> value, ViewBinding viewBinding, Supplier<String> sourceGetter, Consumer<Value> valueReplacer, Consumer<JComponent> viewReplacer) {
                    JComponent view = viewBinding.getView();
                    MouseAdapter mouseAdapter1 = new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                                ConstructorCell constructorCell = new ConstructorCell(sourceGetter.get(), c -> ComponentParser.parse(self, c));
                                viewBinding.setupEditor(constructorCell);
                                viewReplacer.accept(constructorCell.toComponent().getView());
                                constructorCell.addUsage(new Usage() {
                                    @Override
                                    public void removeValue() {
                                        viewReplacer.accept(viewBinding.getView());
                                    }

                                    @Override
                                    public void replaceValue(Value value) {
                                        valueReplacer.accept(value);
                                    }
                                });
                                //valueReplacer.accept(constructorCell);
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
                                ViewBinding targetView = findView(pointInContentPane);
                                JComponent targetComponent = targetView.getView();//(JComponent) contentPane.findComponentAt(pointInContentPane);
                                Point pointInTargetComponent = SwingUtilities.convertPoint(contentPane, pointInContentPane, targetComponent);
                                if(targetComponent != view) {
                                    // Target must support dumping a value on it
                                    Value projection = new Reduction(value.get());

                                    //Value projection = value.get().createProjection();
                                    ViewBinding projectionView = projection.toComponent();
                                    projectionView.setupWorkspace(workspace);
                                    projectionView.getView().setLocation(pointInTargetComponent);
                                    targetView.drop(projection, pointInTargetComponent);
                                    //targetComponent.add(projectionView.getView());

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

                                            origProjectionView.drop(value, location);

                                            //targetComponent.remove(origProjectionView.getView());
                                            projectionView.getView().setLocation(location);
                                            //targetComponent.add(projectionView.getView());
                                            //targetView.drop(projection);
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

                    //contentPane.remove(currentConstructor.getView());
                    removeView(currentConstructor);
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

                //contentPane.add(currentConstructor.getView());
                addView(currentConstructor);
                currentConstructor.getView().revalidate();
                currentConstructor.getView().repaint();
                currentConstructor.getView().requestFocusInWindow();
            }

            private Usage createUsage(boolean isConstructor, Point initialLocation) {
                return new Usage() {
                    ViewBinding viewBinding;
                    Value currentValue;

                    private ViewBinding getViewBinding() {
                        if(viewBinding == null && isConstructor)
                            return currentConstructor;
                        return viewBinding;
                    }

                    @Override
                    public void removeValue() {
                        //contentPane.remove(getViewBinding().getView());
                        removeView(getViewBinding());
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
                        //contentPane.remove(getViewBinding().getView());
                        if(getViewBinding() != null)
                            removeView(getViewBinding());

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

                        Point location = getViewBinding() != null ? getViewBinding().getView().getLocation() : initialLocation;
                        valueAsComponent.setLocation(location);
                        contentPane.add(valueAsComponent);
                        addView(newViewBinding);
                        contentPane.revalidate();
                        contentPane.repaint();

                        value.addUsage(this);

                        if(getViewBinding() == currentConstructor)
                            currentConstructor = null;

                        viewBinding = newViewBinding;
                    }
                };
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                constructDialog(e.getPoint(), createUsage(true, null));
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
