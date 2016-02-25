package org.cirqwizard.layers;

import javafx.scene.canvas.GraphicsContext;
import org.cirqwizard.geom.Point;

public interface LayerElement extends Cloneable
{
    Point getMin();
    Point getMax();
    void move(Point point);
    void rotate(boolean clockwise);
    Object clone() throws CloneNotSupportedException;
    void render(GraphicsContext g);
    boolean isVisible();
}
