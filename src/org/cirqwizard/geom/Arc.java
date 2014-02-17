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
    private RealNumber radius;
    private boolean clockwise;
    private RealNumber start;
    private RealNumber angle;

    public Arc(Point from, Point to, Point center, RealNumber radius, boolean clockwise)
    {
        this.from = from;
        this.to = to;
        this.center = center;
        this.radius = radius;
        this.clockwise = clockwise;

        start = new Line(center, from).angleToX();
        if (to.equals(from))
            angle = MathUtil.TWO_PI;
        else
        {
            angle = new Line(center, to).angleToX();
            angle = calculateAngularDistance(start, angle, clockwise);
        }
    }

    public static RealNumber calculateAngularDistance(RealNumber startAngle, RealNumber endAngle, boolean clockwise)
    {
        if (startAngle.equals(endAngle))
            return MathUtil.TWO_PI;

        RealNumber theta;
        if (!clockwise)
            theta = endAngle.subtract(startAngle);
        else
            theta = startAngle.subtract(endAngle);
        if (theta.lessThan(0))
            theta = theta.add(MathUtil.TWO_PI);
        return theta;
    }

    public Point getCenter()
    {
        return center;
    }

    public RealNumber getRadius()
    {
        return radius;
    }

    public RealNumber getStart()
    {
        return start;
    }

    public RealNumber getAngle()
    {
        return angle;
    }

    public boolean isClockwise()
    {
        return clockwise;
    }

    public RealNumber getEnd(boolean bind)
    {
        RealNumber end;
        if (clockwise)
            end = start.subtract(angle);
        else
            end = start.add(angle);
        if (bind)
            end = MathUtil.bindAngle(end);
        return end;
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
                ", start=" + start +
                ", angle=" + angle +
                '}';
    }
}
