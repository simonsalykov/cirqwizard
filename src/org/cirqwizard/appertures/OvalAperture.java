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

package org.cirqwizard.appertures;

import org.cirqwizard.math.RealNumber;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;


public class OvalAperture extends Aperture
{
    private RealNumber width, height;

    public OvalAperture(RealNumber width, RealNumber height)
    {
        super();
        this.width = width;
        this.height = height;
    }

    public OvalAperture(RealNumber width, RealNumber height, RealNumber holeDiameter)
    {
        super(holeDiameter);
        this.width = width;
        this.height = height;
    }

    public OvalAperture(RealNumber width, RealNumber height, RealNumber holeWidth, RealNumber holeHeight)
    {
        super(holeWidth, holeHeight);
        this.width = width;
        this.height = height;
    }

    public RealNumber getWidth()
    {
        return width;
    }

    public RealNumber getHeight()
    {
        return height;
    }

    public boolean isHorizontal()
    {
        return width.greaterThan(height);
    }

    @Override
    public void render(java.awt.Graphics2D g, int x, int y, double scale)
    {
        double flashX = x;
        double flashY = y;
        double width = this.width.doubleValue() * scale;
        double height = this.height.doubleValue() * scale;
        double d = Math.min(width, height);
        double l = isHorizontal() ? width - height : height - width;
        double xOffset = isHorizontal() ? l / 2 : 0;
        double yOffset = isHorizontal() ? 0 : l / 2;
        double rectX = isHorizontal() ? flashX - l / 2 : flashX - width / 2;
        double rectY = isHorizontal() ? flashY - height / 2 : flashY - l / 2;
        double rectWidth =  isHorizontal() ? l : width;
        double rectHeight =  isHorizontal() ? height : l;

        g.fill(new Ellipse2D.Double(flashX - xOffset - d / 2, flashY + yOffset - d / 2, d, d));
        g.fill(new Ellipse2D.Double(flashX + xOffset - d / 2, flashY - yOffset - d / 2, d, d));
        g.fill(new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight));
    }

    @Override
    public boolean isVisible()
    {
        return height.greaterThan(0) && width.greaterThan(0);
    }

    @Override
    public RealNumber getWidth(RealNumber angle)
    {
        return width.greaterOrEqualTo(height) ? width : height;
    }

    @Override
    public Aperture rotate(boolean clockwise)
    {

        return new OvalAperture(height, width);
    }
}
