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

public class CircularAperture extends Aperture
{
    private int diameter;

    public CircularAperture(int diameter)
    {
        super();
        this.diameter = diameter;
    }

    public CircularAperture(int diameter, int holeDiameter)
    {
        super(holeDiameter);
        this.diameter = diameter;
    }

    public CircularAperture(int diameter, int holeWidth, int holeHeight)
    {
        super(holeWidth, holeHeight);
        this.diameter = diameter;
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
    public Aperture rotate(boolean clockwise)
    {
        return this;
    }

    @Override
    public int getCircumRadius()
    {
        return diameter / 2;
    }

    @Override
    public Polygon createPolygon(int x, int y, int inflation)
    {
        return (Polygon) VectorToolPathGenerator.factory.createPoint(new Coordinate(x, y)).buffer(diameter / 2 + inflation,
                diameter / 1000 * 5);
    }

    public int getRectWidth()
    {
        // pythagorean theorem
        return (int) Math.sqrt(diameter * diameter / 2);
    }
}
