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


public class Arc extends Curve
{
    private Point center;
    private int radius;
    private boolean clockwise;
    private double start;
    private double angle;

    public Arc(Point from, Point to, Point center, int radius, boolean clockwise)
    {
        this.from = from;
        this.to = to;
        this.center = center;
        this.radius = radius;
        this.clockwise = clockwise;

        updateAngles();
    }

    private void updateAngles()
    {
        start = new Line(center, from).angleToX();
        if (to.equals(from))
            angle = Math.PI * 2;
        else
        {
            angle = new Line(center, to).angleToX();
            angle = calculateAngularDistance(start, angle, clockwise);
        }
    }

    public static double calculateAngularDistance(double startAngle, double endAngle, boolean clockwise)
    {
        if (startAngle == endAngle)
            return Math.PI * 2;

        double theta;
        if (!clockwise)
            theta = endAngle - startAngle;
        else
            theta = startAngle - endAngle;
        if (theta < 0)
            theta += Math.PI * 2;
        return theta;
    }

    @Override
    public Curve reverse()
    {
        return new Arc(to, from, center, radius, !clockwise);
    }

    @Override
    public void setFrom(Point from)
    {
        super.setFrom(from);
        updateAngles();
    }

    @Override
    public void setTo(Point to)
    {
        super.setTo(to);
        updateAngles();
    }

    public Point getCenter()
    {
        return center;
    }

    public int getRadius()
    {
        return radius;
    }

    public double getStart()
    {
        return start;
    }

    public double getAngle()
    {
        return angle;
    }

    public boolean isClockwise()
    {
        return clockwise;
    }

    public double getEnd(boolean bind)
    {
        double end;
        if (clockwise)
            end = start - angle;
        else
            end = start + angle;
        if (bind)
            end = MathUtil.bindAngle(end);
        return end;
    }

    public int getCircumreference()
    {
        return (int)(getAngle() * radius);
    }

    @Override
    public String toString()
    {
        return "Arc{" +
                "from=" + from +
                ", to=" + to +
                ", center=" + center +
                ", radius=" + radius +
                ", clockwise=" + clockwise +
                ", start=" + getStart() +
                ", end=" + getEnd(false) +
                '}';
    }
}
