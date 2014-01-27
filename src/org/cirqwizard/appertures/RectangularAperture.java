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

import org.cirqwizard.math.MathUtil;
import org.cirqwizard.math.RealNumber;


public class RectangularAperture extends Aperture
{
    private RealNumber dimensions[] = new RealNumber[2];

    public RectangularAperture(RealNumber width, RealNumber height)
    {
        super();
        this.dimensions[0] = width;
        this.dimensions[1] = height;
    }

    public RectangularAperture(RealNumber width, RealNumber height, RealNumber holeDiameter)
    {
        super(holeDiameter);
        this.dimensions[0] = width;
        this.dimensions[1] = height;
    }

    public RectangularAperture(RealNumber width, RealNumber height, RealNumber holeWidth, RealNumber holeHeight)
    {
        super(holeWidth, holeHeight);
        this.dimensions[0] = width;
        this.dimensions[1] = height;
    }

    public RealNumber[] getDimensions()
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
        return dimensions[0].greaterThan(0) && dimensions[1].greaterThan(0);
    }

    @Override
    public RealNumber getWidth(RealNumber angle)
    {
        return MathUtil.max(dimensions[0], dimensions[1]);
    }

}
