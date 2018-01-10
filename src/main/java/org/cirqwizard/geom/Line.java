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

public class Line extends Curve
{
    private Double angleToX = null;

    public Line(Point from, Point to)
    {
        this.from = from;
        this.to = to;
    }

    public Line reverse()
    {
        return new Line(to, from);
    }

    public double angleToX()
    {
        if (angleToX == null)
        {
            Point vector = to.subtract(from);
            angleToX = Math.atan2(vector.getY(), vector.getX());
            if (Math.abs(angleToX - Math.PI) < Math.PI / 360)
                angleToX = -Math.PI;
        }
        return angleToX;
    }

    public Line offset(Point point)
    {
        return new Line(from.add(point), to.add(point));
    }

    public Line offsetFrom(int offset)
    {
        return new Line(offset(from, to, offset), to);
    }

    public Line offsetTo(int offset)
    {
        return new Line(from, offset(to, from, offset));
    }

    private Point offset(Point start, Point end, int offset)
    {
        double length = length();
        if (offset >= length)
            throw new IllegalArgumentException("Offset in line cannot be more or equal current distance");

        double t = offset / length;
        int toX = (int)((1 - t) * start.getX() + t * end.getX());
        int toY = (int)((1 - t) * start.getY() + t * end.getY());
        return new Point(toX, toY);
    }

    public double length()
    {
        return from.distanceTo(to);
    }

    @Override
    public String toString()
    {
        return "Line{" +
                "from=" + getFrom() +
                ", to=" + getTo() +
                ", ang=" + angleToX() +
                '}';
    }
}
