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


public class CircularAperture extends Aperture
{
    private RealNumber diameter;

    public CircularAperture(RealNumber diameter)
    {
        super();
        this.diameter = diameter;
    }

    public CircularAperture(RealNumber diameter, RealNumber holeDiameter)
    {
        super(holeDiameter);
        this.diameter = diameter;
    }

    public CircularAperture(RealNumber diameter, RealNumber holeWidth, RealNumber holeHeight)
    {
        super(holeWidth, holeHeight);
        this.diameter = diameter;
    }

    public RealNumber getDiameter()
    {
        return diameter;
    }

    @Override
    public void render(java.awt.Graphics2D g, int x, int y, double scale)
    {
        int diameter = (int) (this.diameter.doubleValue() * scale);
        g.fillArc(x - diameter / 2, y - diameter / 2, diameter, diameter, 0, 360);
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

    @Override
    public Aperture rotate(boolean clockwise)
    {
        return this;
    }


}
