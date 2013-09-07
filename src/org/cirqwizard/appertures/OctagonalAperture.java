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

import java.awt.*;


public class OctagonalAperture extends Aperture
{
    private RealNumber diameter;

    public OctagonalAperture(RealNumber diameter)
    {
        super();
        this.diameter = diameter;
    }

    public OctagonalAperture(RealNumber diameter, RealNumber holeDiameter)
    {
        super(holeDiameter);
        this.diameter = diameter;
    }

    public OctagonalAperture(RealNumber diameter, RealNumber holeWidth, RealNumber holeHeight)
    {
        super(holeWidth, holeHeight);
        this.diameter = diameter;
    }

    @Override
    public Aperture rotate(boolean clockwise)
    {
        return this;
    }

    public RealNumber getDiameter()
    {
        return diameter;
    }

    @Override
    public void render(Graphics2D g, int x, int y, double scale)
    {
        int edgeOffset = (int) (diameter.doubleValue() * (Math.pow(2, 0.5) - 1) / 2);
        int centerOffset = (int) (diameter.doubleValue() * 0.5);
        int xPoints[] = {centerOffset + x, edgeOffset + x, -edgeOffset + x, -centerOffset + x, -centerOffset + x, -edgeOffset + x, edgeOffset + x, centerOffset + x};
        int yPoints[] = {edgeOffset + y, centerOffset + y, centerOffset + y, edgeOffset + y, -edgeOffset + y, -centerOffset + y, -centerOffset + y, -edgeOffset + y};
        g.fillPolygon(xPoints, yPoints, xPoints.length);
    }

    @Override
    public boolean isVisible()
    {
        return diameter.greaterThan(0);
    }

    @Override
    public RealNumber getWidth(RealNumber angle)
    {
        return diameter;
    }
}
