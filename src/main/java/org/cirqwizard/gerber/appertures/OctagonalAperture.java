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

package org.cirqwizard.gerber.appertures;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import org.cirqwizard.generation.VectorToolPathGenerator;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

public class OctagonalAperture extends Aperture
{
    private int diameter;

    public OctagonalAperture(int diameter)
    {
        super();
        this.diameter = diameter;
    }

    public OctagonalAperture(int diameter, int holeDiameter)
    {
        super(holeDiameter);
        this.diameter = diameter;
    }

    public OctagonalAperture(int diameter, int holeWidth, int holeHeight)
    {
        super(holeWidth, holeHeight);
        this.diameter = diameter;
    }

    @Override
    public Aperture rotate(boolean clockwise)
    {
        return this;
    }

    public int getDiameter()
    {
        return diameter;
    }

    @Override
    public boolean isVisible()
    {
        return diameter > 0;
    }

    @Override
    public int getWidth()
    {
        return diameter;
    }

    @Override
    public int getHeight()
    {
        return diameter;
    }

    @Override
    public int getCircumRadius()
    {
        return diameter / 2;
    }

    @Override
    public Polygon createPolygon(int x, int y, int inflation)
    {
        double edgeOffset = (Math.pow(2, 0.5) - 1) / 2 * (diameter + inflation * 2);
        double centerOffset = 0.5 * (diameter + inflation * 2);

        return VectorToolPathGenerator.factory.createPolygon(new Coordinate[]{
                new Coordinate(centerOffset + x, edgeOffset + y),
                new Coordinate(edgeOffset + x, centerOffset + y),
                new Coordinate(-edgeOffset + x, centerOffset + y),
                new Coordinate(-centerOffset + x, edgeOffset + y),
                new Coordinate(-centerOffset + x, -edgeOffset + y),
                new Coordinate(-edgeOffset + x, -centerOffset + y),
                new Coordinate(edgeOffset + x, -centerOffset + y),
                new Coordinate(centerOffset + x, -edgeOffset + y),
                new Coordinate(centerOffset + x, edgeOffset + y)
        });
    }
}
