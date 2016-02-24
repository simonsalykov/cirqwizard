package org.cirqwizard.layers;

import org.cirqwizard.geom.Point;

public interface LayerElement
{
    Point getMin();
    Point getMax();
    void move(Point point);
    void rotate(boolean clockwise);
}
