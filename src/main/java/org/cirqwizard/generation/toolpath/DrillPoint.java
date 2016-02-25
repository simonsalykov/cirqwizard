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
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Point;
import org.cirqwizard.layers.LayerElement;


public class DrillPoint extends CuttingToolpath implements LayerElement
{
    private Point point;

    public DrillPoint(Point point, int diameter)
    {
        super(((diameter + 50) / 100 * 100));    // Rounding to tenth of millimeter
        this.point = point;
    }

    public Point getPoint()
    {
        return point;
    }

    public void setPoint(Point point)
    {
        this.point = point;
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
    public boolean isVisible()
    {
        return true;
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
            point = new Point(point.getY(), -point.getX());
        else
            point = new Point(-point.getY(), point.getX());
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public void render(GraphicsContext g)
    {
        g.fillOval(getPoint().getX() - getToolDiameter() / 2,
                getPoint().getY() - getToolDiameter() / 2,
                getToolDiameter(), getToolDiameter());
    }

    @Override
    public Curve getCurve()
    {
        return null;
    }
}
