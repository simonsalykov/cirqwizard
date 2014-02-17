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


public class Point
{
    private RealNumber x;
    private RealNumber y;

    public Point(RealNumber x, RealNumber y)
    {
        this.x = x;
        this.y = y;
    }

    public Point(int x, int y)
    {
        this(new RealNumber(x), new RealNumber(y));
    }


    public RealNumber getX()
    {
        return x;
    }

    public RealNumber getY()
    {
        return y;
    }

    public RealNumber distanceTo(Point p)
    {
        if (equals(p))
            return MathUtil.ZERO;
        double x = this.x.doubleValue() - p.x.doubleValue();
        double y = this.y.doubleValue() - p.y.doubleValue();
        return new RealNumber(Math.sqrt(x * x + y * y));
    }

    public Point add(Point p)
    {
        return new Point(this.x.add(p.getX()), this.y.add(p.getY()));
    }

    public Point subtract(Point p)
    {
        return new Point(this.x.subtract(p.getX()), this.y.subtract(p.getY()));
    }

    public Point divide(RealNumber scalar)
    {
        return new Point(x.divide(scalar), y.divide(scalar));
    }

    public Point rotateRelativeToOrigin(boolean clockwise)
    {
        if (clockwise)
            return new Point(y, x.negate());
        else
            return new Point(y.negate(), x);
    }

    @Override
    public String toString()
    {
        return "Point{" +
                "x=" + x.toString() +
                ", y=" + y.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        double x = this.x.doubleValue() - point.x.doubleValue();
        double y = this.y.doubleValue() - point.y.doubleValue();
        return Math.sqrt(x * x + y * y) < MathUtil.MICRON.doubleValue();
    }

    @Override
    public int hashCode()
    {
        int result = x != null ? x.hashCode() : 0;
        result = 31 * result + (y != null ? y.hashCode() : 0);
        return result;
    }
}
