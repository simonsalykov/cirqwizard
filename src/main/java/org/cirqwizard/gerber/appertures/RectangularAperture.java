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

public class RectangularAperture extends Aperture
{
    private int dimensions[] = new int[2];

    public RectangularAperture(int width, int height)
    {
        super();
        this.dimensions[0] = width;
        this.dimensions[1] = height;
    }

    public RectangularAperture(int width, int height, int holeDiameter)
    {
        super(holeDiameter);
        this.dimensions[0] = width;
        this.dimensions[1] = height;
    }

    public RectangularAperture(int width, int height, int holeWidth, int holeHeight)
    {
        super(holeWidth, holeHeight);
        this.dimensions[0] = width;
        this.dimensions[1] = height;
    }

    public int[] getDimensions()
    {
        return dimensions;
    }

    @Override
    public Aperture rotate(boolean clockwise)
    {
        return new RectangularAperture(dimensions[1], dimensions[0], holeDimensions[1], holeDimensions[0]);
    }

    @Override
    public boolean isVisible()
    {
        return dimensions[0] > 0 && dimensions[1] > 0;
    }

    @Override
    public int getWidth()
    {
        return dimensions[0];
    }

    @Override
    public int getHeight()
    {
        return dimensions[1];
    }

    @Override
    public int getCircumRadius()
    {
        int width = dimensions[0] / 2;
        int height = dimensions[1] / 2;
        return (int) Math.sqrt(width * width + height * height);
    }

    @Override
    public Polygon createPolygon(int x, int y, int inflation)
    {
        return VectorToolPathGenerator.factory.createPolygon(new Coordinate[]{new Coordinate(x - dimensions[0] / 2 - inflation, y - dimensions[1] / 2 - inflation),
                new Coordinate(x + dimensions[0] / 2 + inflation, y - dimensions[1] / 2 - inflation),
                new Coordinate(x + dimensions[0] / 2 + inflation, y + dimensions[1] / 2 + inflation),
                new Coordinate(x - dimensions[0] / 2 - inflation, y + dimensions[1] / 2 + inflation),
                new Coordinate(x - dimensions[0] / 2 - inflation, y - dimensions[1] / 2 - inflation)});
    }
}
