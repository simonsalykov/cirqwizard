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
import org.cirqwizard.toolpath.DrillPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class DrillingLayer extends Layer
{
    private ArrayList<DrillPoint> drillPoints;

    public List<DrillPoint> getToolpaths()
    {
        return drillPoints;
    }

    public void setDrillPoints(ArrayList<DrillPoint> drillPoints)
    {
        this.drillPoints = drillPoints;
    }

    public List<Integer> getDrillDiameters()
    {
        return drillPoints.stream().map(DrillPoint::getToolDiameter).distinct().sorted().collect(Collectors.toList());
    }

    @Override
    public void rotate(boolean clockwise)
    {
        for (DrillPoint d : drillPoints)
        {
            if (clockwise)
                d.setPoint(new Point(d.getPoint().getY(), -d.getPoint().getX()));
            else
                d.setPoint(new Point(-d.getPoint().getY(), d.getPoint().getX()));
        }
    }

    @Override
    public void move(Point point)
    {
        for (DrillPoint d : drillPoints)
            d.setPoint(d.getPoint().add(point));
    }

    @Override
    public Point getMinPoint()
    {
        Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (DrillPoint d : drillPoints)
        {
            if (d.getPoint().getX() < min.getX())
                min = new Point(d.getPoint().getX(), min.getY());
            if (d.getPoint().getY() < min.getY())
                min = new Point(min.getX(), d.getPoint().getY());
        }
        return min;
    }

    @Override
    public void clearSelection()
    {
    }

}
