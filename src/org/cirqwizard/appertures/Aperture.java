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


public abstract class Aperture
{
    public enum HoleType
    {
        NONE,
        CIRCULAR,
        RECTANGULAR
    }

    protected HoleType holeType;
    protected RealNumber holeDimensions[] = new RealNumber[2];

    public Aperture()
    {
        this.holeType = HoleType.NONE;
    }

    public Aperture(RealNumber holeDiameter)
    {
        this.holeType = HoleType.CIRCULAR;
        this.holeDimensions[0] = holeDiameter;
    }

    public Aperture(RealNumber holeWidth, RealNumber holeHeight)
    {
        this.holeType = HoleType.RECTANGULAR;
        this.holeDimensions[0] = holeWidth;
        this.holeDimensions[1] = holeHeight;
    }

    public abstract Aperture rotate(boolean clockwise);

    public abstract boolean isVisible();

    public abstract RealNumber getWidth(RealNumber angle);

}
