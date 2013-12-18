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

    @Override
    public void render(java.awt.Graphics2D g, int x, int y, double scale)
    {
        double flashX = x;
        double flashY = y;
        double width = this.width.doubleValue() * scale;
        double height = this.height.doubleValue() * scale;
        double d, l;

        if (width > height)
        {
            d = height;
            l = width - height;

            g.fill(new Ellipse2D.Double(flashX - l / 2 - d / 2, flashY - d / 2, d, d));
            g.fill(new Ellipse2D.Double(flashX + l / 2 - d / 2, flashY - d / 2, d, d));
            g.fill(new Rectangle2D.Double(flashX - l / 2, flashY - height / 2, l, height));

        }
        else if (width < height)
        {
            d = width;
            l = height - width;

            g.fill(new Ellipse2D.Double(flashX - d / 2, flashY + l / 2 - d / 2, d, d));
            g.fill(new Ellipse2D.Double(flashX - d / 2, flashY - l / 2 - d / 2, d, d));
            g.fill(new Rectangle2D.Double(flashX - width / 2, flashY - l / 2, width, l));
        }
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
