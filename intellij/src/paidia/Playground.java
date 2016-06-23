package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Playground extends JPanel {
    private MouseAdapter mouseAdapter;

    public Playground(MouseToolProvider mouseToolProvider) {
        mouseToolProvider.setCanvas(this);

        setLayout(null);

        ComponentAdapter componentAdapter = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                e.getComponent().revalidate();
                e.getComponent().repaint();
            }
        };

        addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                revalidate();
                repaint(e.getChild().getBounds());

                e.getChild().addComponentListener(componentAdapter);

                if(e.getChild() instanceof PlaygroundElement) {
                    ((PlaygroundElement) e.getChild()).setup(Playground.this);
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                revalidate();
                repaint(e.getChild().getBounds());

                e.getChild().removeComponentListener(componentAdapter);

                if(e.getChild() instanceof ValueView) {
                    ((Container)e.getChild()).removeContainerListener(this);
                    ((PlaygroundElement)e.getChild()).release();
                }
            }
        });

        mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseToolProvider.getMouseTool().mouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseToolProvider.getMouseTool().mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseToolProvider.getMouseTool().mouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                mouseToolProvider.getMouseTool().mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseToolProvider.getMouseTool().mouseExited(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                mouseToolProvider.getMouseTool().mouseWheelMoved(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseToolProvider.getMouseTool().mouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseToolProvider.getMouseTool().mouseMoved(e);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);

        addContainerListener(this);

        JLabel label = new JLabel("sdfsdfds");
        label.setLocation(50, 50);
        label.setSize(label.getPreferredSize());
        add(label);
    }

    private ComponentAdapter componentAdapter = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            e.getComponent().revalidate();
            e.getComponent().repaint();
        }
    };

    private ContainerAdapter containerAdapter = new ContainerAdapter() {
        @Override
        public void componentAdded(ContainerEvent e) {
            revalidate();
            repaint(e.getChild().getBounds());

            e.getChild().addComponentListener(componentAdapter);
            addContainerListener((JComponent) e.getChild());
            registerComponentForTools((JComponent) e.getChild());
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            revalidate();
            repaint(e.getChild().getBounds());

            unregisterComponentForTools((JComponent) e.getChild());
            e.getChild().removeComponentListener(componentAdapter);
            ((Container)e.getChild()).removeContainerListener(this);
        }
    };

    private void addContainerListener(JComponent component) {
        component.addContainerListener(containerAdapter);
        for(int i = 0; i < component.getComponentCount(); i++)
            addContainerListener((ContainerListener) component.getComponent(i));
    }

    private void registerComponentForTools(JComponent component) {
        component.addMouseListener(mouseAdapter);
        component.addMouseMotionListener(mouseAdapter);
        component.addMouseWheelListener(mouseAdapter);
    }

    private void unregisterComponentForTools(JComponent component) {
        component.removeMouseListener(mouseAdapter);
        component.removeMouseMotionListener(mouseAdapter);
        component.removeMouseWheelListener(mouseAdapter);
    }
}
