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

package org.cirqwizard.gerber.appertures.macro;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import org.cirqwizard.generation.VectorToolPathGenerator;
import org.cirqwizard.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class MacroOutline extends MacroPrimitive
{
    private ArrayList<Point> points = new ArrayList<>();

    public MacroOutline()
    {
    }

    public MacroOutline(ArrayList<Point> points, int rotationAngle)
    {
        super(rotationAngle);
        this.points = points;
    }

    public void addPoint(Point point)
    {
        points.add(point);
    }

    public List<Point> getPoints()
    {
        return points;
    }

    public List<Point> getTranslatedPoints()
    {
        ArrayList<Point> result = new ArrayList<>();
        for (Point p : points)
            result.add(translate(p));

        return result;
    }

    @Override
    public MacroPrimitive clone()
    {
        return new MacroOutline(points, getRotationAngle());
    }

    @Override
    public Polygon createPolygon(int x, int y, int inflation)
    {
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < getTranslatedPoints().size(); i++)
        {
            Point point = getPoints().get(i);
            coordinates.add(new Coordinate(point.getX() + x, point.getY() + y));
        }
        coordinates.add(coordinates.get(0));

        Coordinate[] c = new Coordinate[coordinates.size()];
        coordinates.toArray(c);
        return (Polygon) VectorToolPathGenerator.factory.createPolygon(c).buffer(inflation);
    }
}
