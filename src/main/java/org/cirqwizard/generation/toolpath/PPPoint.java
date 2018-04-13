/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cirqwizard.generation.toolpath;

import javafx.scene.canvas.GraphicsContext;
import org.cirqwizard.geom.Point;
import org.cirqwizard.layers.LayerElement;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.settings.ApplicationConstants;


public class PPPoint implements LayerElement
{
    private ComponentId id;
    private Point point;
    private int angle;
    private String name;

    public PPPoint(ComponentId id, Point point, int angle, String name)
    {
        this.id = id;
        this.point = point;
        this.angle = angle;
        this.name = name;
    }

    public ComponentId getId()
    {
        return id;
    }

    public void setId(ComponentId id)
    {
        this.id = id;
    }

    public Point getPoint()
    {
        return point;
    }

    public void setPoint(Point point)
    {
        this.point = point;
    }

    public int getAngle()
    {
        return angle;
    }

    public void setAngle(int angle)
    {
        this.angle = angle;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public Point getMin()
    {
        return point;
    }

    @Override
    public Point getMax()
    {
        return point;
    }

    @Override
    public void move(Point point)
    {
        this.point = this.point.add(point);
    }

    @Override
    public void rotate(boolean clockwise)
    {
        if (clockwise)
        {
            point = new Point(point.getY(), -point.getX());
            angle += 90 * ApplicationConstants.RESOLUTION;
        }
        else
        {
            point = new Point(-point.getY(), point.getX());
            angle -= 90 * ApplicationConstants.RESOLUTION;
        }
        angle %= 360 * ApplicationConstants.RESOLUTION;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public void render(GraphicsContext g)
    {

    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean isVisible()
    {
        return false;
    }
}
