package paidia;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

public class RoundedBorder extends AbstractBorder {
    private int arcSize;

    public RoundedBorder() {
        this(25);
    }

    public RoundedBorder(int arcSize) {
        this.arcSize = arcSize;
    }

    public int getArcSize() {
        return arcSize;
    }

    public void setArcSize(int arcSize) {
        this.arcSize = arcSize;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Dimension arcs = new Dimension(arcSize, arcSize);
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        //Draws the rounded panel with borders.
        graphics.setColor(c.getBackground());
        graphics.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);//paint background
        graphics.setColor(c.getForeground());
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);//paint borderd
        graphics.drawString("sdffsd", 0, 0);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        if(c instanceof ScopeView)
            return new Insets(5, 5, 5, 5);
        else if(c instanceof JPopupMenu)
            return new Insets(5, 5, 5, 5);

        return new Insets(0, 0, 0, 0);
    }

    public void adjustArcSize(JComponent c) {
        int distanceToFarthestParentWithRoundedBorder = distanceToFarthestParentWithRoundedBorder((JComponent) c.getParent(), 0);

        int arcSize = getArcSize();
        int newArcSize = (int)(((double)(arcSize - distanceToFarthestParentWithRoundedBorder * 4)) / arcSize * arcSize);
        setArcSize(newArcSize);
    }

    private int distanceToFarthestParentWithRoundedBorder(JComponent c, int distance) {
        if(c == null)
            return distance;
        if(c.getBorder() instanceof RoundedBorder)
            distance++;
        if(c.getParent() instanceof JComponent)
            return distanceToFarthestParentWithRoundedBorder((JComponent) c.getParent(), distance);
        return distance;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}
