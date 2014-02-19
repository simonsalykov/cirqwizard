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

public class Point
{
    private int x;
    private int y;

    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public double distanceTo(Point p)
    {
        double x = this.x - p.x;
        double y = this.y - p.y;
        return Math.sqrt(x * x + y * y);
    }

    public Point add(Point p)
    {
        return new Point(this.x + p.x, this.y + p.y);
    }

    public Point subtract(Point p)
    {
        return new Point(this.x - p.x, this.y - p.y);
    }

    public Point round()
    {
        return round(10);
    }

    public Point round(int roundTo)
    {
        return new Point((x + roundTo / 2) / roundTo * roundTo, (y + roundTo / 2) / roundTo * roundTo);
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
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return equals((Point) o, 1);
    }

    public boolean equals(Point o, double precision)
    {
        return distanceTo(o) < precision;
    }

    @Override
    public int hashCode()
    {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
