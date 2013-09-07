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

package org.cirqwizard.geom;

import org.cirqwizard.math.MathUtil;
import org.cirqwizard.math.RealNumber;


public class Line extends Curve
{
    private RealNumber angleToX = null;

    public Line(Point from, Point to)
    {
        this.from = from;
        this.to = to;

    }

    public Line reverse()
    {
        return new Line(to, from);
    }

    public RealNumber angleToX()
    {
        if (angleToX == null)
        {
            if (from.getX().equals(to.getX()))
            {
                if (from.getY().lessThan(to.getY()))
                    angleToX = MathUtil.HALF_PI;
                else
                    angleToX = MathUtil.HALF_PI.negate();
            }
            else if (from.getY().equals(to.getY()))
            {
                if (from.getX().lessThan(to.getX()))
                    angleToX = MathUtil.ZERO;
                else
                    angleToX = MathUtil.MINUS_PI;
            }
            else
            {
                Point vector = to.subtract(from);
                angleToX = MathUtil.atan2(vector.getY(), vector.getX());
                if (angleToX.equals(MathUtil.PI))
                    angleToX = MathUtil.MINUS_PI;
            }
        }
        return angleToX;
    }

}
