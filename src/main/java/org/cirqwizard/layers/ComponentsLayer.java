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

package org.cirqwizard.layers;

import org.cirqwizard.geom.Point;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.generation.toolpath.PPPoint;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;


public class ComponentsLayer extends Layer
{
    private List<PPPoint> points;

    public List<PPPoint> getPoints()
    {
        return points;
    }

    public void setPoints(List<PPPoint> points)
    {
        this.points = points;
    }

    public List<ComponentId> getComponentIds()
    {
        List<ComponentId> ids = new ArrayList<>();
        for (PPPoint p : points)
            if (!ids.contains(p.getId()))
                ids.add(p.getId());
        return ids;
    }

    private int bindAngle(int angle)
    {
        if (angle >= 360)
            return angle - 360;
        if (angle < 0)
            return angle + 360;
        return angle;
    }

    @Override
    public void rotate(boolean clockwise)
    {
        for (PPPoint p : points)
        {
            if (clockwise)
            {
                p.setPoint(new Point(p.getPoint().getY(), -p.getPoint().getX()));
                p.setAngle(bindAngle(p.getAngle() + 90));
            }
            else
            {
                p.setPoint(new Point(-p.getPoint().getY(), p.getPoint().getX()));
                p.setAngle(bindAngle(p.getAngle() - 90));
            }
        }
    }

    @Override
    public void move(Point point)
    {
        for (PPPoint p : points)
            p.setPoint(p.getPoint().add(point));
    }

    @Override
    public Point getMinPoint()
    {
        int minX = points.stream().mapToInt(p -> p.getPoint().getX()).min().getAsInt();
        int minY = points.stream().mapToInt(p -> p.getPoint().getY()).min().getAsInt();
        return new Point(minX, minY);
    }

    @Override
    public Point getMaxPoint()
    {
        int maxX = points.stream().mapToInt(p -> p.getPoint().getX()).max().getAsInt();
        int maxY = points.stream().mapToInt(p -> p.getPoint().getY()).max().getAsInt();
        return new Point(maxX, maxY);
    }

    @Override
    public List<? extends Toolpath> getToolpaths()
    {
        return points;
    }

}
