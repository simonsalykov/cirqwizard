package org.cirqwizard.geom;

import java.io.Serializable;

public class Rect implements Serializable
{
    private Point center;
    private int width;
    private int height;

    public Rect(Point center, int width, int height)
    {
        this.center = center;
        this.width = width;
        this.height = height;
    }

    public Point getCenter()
    {
        return center;
    }

    public void setCenter(Point center)
    {
        this.center = center;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getLeftX()
    {
        return getCenter().getX() - getWidth() / 2;
    }

    public int getRightX()
    {
        return getCenter().getX() + getWidth() / 2;
    }

    public int getTopY()
    {
        return getCenter().getY() + getHeight() / 2;
    }

    public int getBottomY()
    {
        return getCenter().getY() - getHeight() / 2;
    }
}
