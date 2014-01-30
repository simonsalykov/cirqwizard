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
