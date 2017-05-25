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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import org.cirqwizard.generation.VectorToolPathGenerator;

public class OvalAperture extends Aperture
{
    private int width, height;

    public OvalAperture(int width, int height)
    {
        super();
        this.width = width;
        this.height = height;
    }

    public OvalAperture(int width, int height, int holeDiameter)
    {
        super(holeDiameter);
        this.width = width;
        this.height = height;
    }

    public OvalAperture(int width, int height, int holeWidth, int holeHeight)
    {
        super(holeWidth, holeHeight);
        this.width = width;
        this.height = height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public boolean isHorizontal()
    {
        return width > height;
    }

    @Override
    public boolean isVisible()
    {
        return height > 0 && width > 0;
    }

    @Override
    public Aperture rotate(boolean clockwise)
    {
        return new OvalAperture(height, width);
    }

    @Override
    public int getCircumRadius()
    {
        return (int) Math.sqrt(width * width + height * height);
    }

    @Override
    public Polygon createPolygon(int x, int y, int inflation)
    {
        double width = Math.max(this.width + inflation * 2, 0);
        double height = Math.max(this.height + inflation * 2, 0);
        double d = Math.min(width, height);
        double l = isHorizontal() ? width - height : height - width;
        double xOffset = isHorizontal() ? l / 2 : 0;
        double yOffset = isHorizontal() ? 0 : l / 2;
        double rectX = isHorizontal() ? x - l / 2 : x - width / 2;
        double rectY = isHorizontal() ? y - height / 2 : y - l / 2;
        double rectWidth = isHorizontal() ? l : width;
        double rectHeight = isHorizontal() ? height : l;

        Polygon c1 = (Polygon) VectorToolPathGenerator.factory.
                createPoint(new Coordinate(x - xOffset - d / 2, y + yOffset - d / 2)).buffer(d / 2);
        Polygon c2 = (Polygon) VectorToolPathGenerator.factory.
                createPoint(new Coordinate(x + xOffset - d / 2, y - yOffset - d / 2)).buffer(d / 2);
        Polygon rect = VectorToolPathGenerator.factory.createPolygon(new Coordinate[]{
                new Coordinate(rectX, rectY),
                new Coordinate(rectX + rectWidth, rectY),
                new Coordinate(rectX + rectWidth, rectY + rectHeight),
                new Coordinate(rectX, rectY + rectHeight),
                new Coordinate(rectX, rectY)});

        return (Polygon) VectorToolPathGenerator.factory.createGeometryCollection(new Geometry[]{c1, rect, c2}).buffer(0);
    }
}
