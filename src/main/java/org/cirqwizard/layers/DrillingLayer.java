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
import org.cirqwizard.generation.toolpath.DrillPoint;

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
        int minX = drillPoints.stream().mapToInt(p -> p.getPoint().getX()).min().getAsInt();
        int minY = drillPoints.stream().mapToInt(p -> p.getPoint().getY()).min().getAsInt();
        return new Point(minX, minY);
    }

    @Override
    public Point getMaxPoint()
    {
        int maxX = drillPoints.stream().mapToInt(p -> p.getPoint().getX()).max().getAsInt();
        int maxY = drillPoints.stream().mapToInt(p -> p.getPoint().getY()).max().getAsInt();
        return new Point(maxX, maxY);
    }

}
