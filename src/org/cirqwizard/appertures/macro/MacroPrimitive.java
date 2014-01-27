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

package org.cirqwizard.appertures.macro;

import org.cirqwizard.geom.Point;
import org.cirqwizard.math.RealNumber;

public abstract class MacroPrimitive
{
    private RealNumber rotationAngle;

    protected MacroPrimitive()
    {
        this(null);
    }

    protected MacroPrimitive(RealNumber rotationAngle)
    {
        this.rotationAngle = rotationAngle;
    }

    protected Point translate(Point p)
    {
        if (rotationAngle != null && rotationAngle.equals(new RealNumber(0)))
            return p;
        double theta = Math.toRadians(rotationAngle.doubleValue());
        double x = p.getX().doubleValue() * Math.cos(theta) - p.getY().doubleValue() * Math.sin(theta);
        double y = p.getX().doubleValue() * Math.sin(theta) + p.getY().doubleValue() * Math.cos(theta);
        return new Point(new RealNumber(x), new RealNumber(y));
    }

    public RealNumber getRotationAngle()
    {
        return rotationAngle;
    }

    public void setRotationAngle(RealNumber rotationAngle)
    {
        this.rotationAngle = rotationAngle;
    }
}
