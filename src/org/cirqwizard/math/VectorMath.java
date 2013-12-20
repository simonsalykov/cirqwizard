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
    public static Point scalarMultiply(Point v, double s)
    {
        return new Point((int)(s * v.getX()), (int)(s * v.getY()));
    }

    public static double dotProduct(Point v1, Point v2)
    {
        return (double)v1.getX() * v2.getX() + (double)v1.getY() * v2.getY();
    }

    public static Point rotateClockwise(Point v)
    {
        return new Point(v.getY(), -v.getX());
    }

    public static Point rotateCounterclockwise(Point v)
    {
        return new Point(-v.getY(), v.getX());
    }

    public static Point unitVector(Point v)
    {
        double len = new Point(0, 0).distanceTo(v);
        return new Point((int)((double)v.getX() / len), (int)((double)v.getY() / len));
    }
}
