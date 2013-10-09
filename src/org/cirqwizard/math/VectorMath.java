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

package org.cirqwizard.math;

import org.cirqwizard.geom.Point;


public class VectorMath
{
    public static Point vecMul(Point v, double s)
    {
        return new Point(new RealNumber(v.getX().doubleValue() * s), new RealNumber(v.getY().doubleValue() * s));
    }

    public static RealNumber vecDot(Point v1, Point v2)
    {
        return v1.getX().multiply(v2.getX()).add(v1.getY().multiply(v2.getY()));
    }

    public static Point vecRot90CW(Point v)
    {
        return new Point(v.getY(), v.getX().negate());
    }

    public static Point vecRot90CCW(Point v)
    {
        return new Point(v.getY().negate(), v.getX());
    }

    public static Point vecUnit(Point v)
    {
        RealNumber len = new RealNumber(Math.sqrt(v.getX().multiply(v.getX()).add(v.getY().multiply(v.getY())).doubleValue()));
        return new Point(v.getX().divide(len), v.getY().divide(len));
    }
}
