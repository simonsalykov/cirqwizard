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

package org.cirqwizard.gerber;

import org.cirqwizard.appertures.Aperture;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;

public class CircularShape extends InterpolatingShape
{
    private Arc arc;

    public CircularShape(int fromX, int fromY, int toX, int toY, int centerX, int centerY,
                         boolean clockwise, Aperture aperture, Polarity polarity)
    {
        super(polarity);
        Point from = new Point(fromX, fromY);
        Point center = new Point(centerX, centerY);
        arc = new Arc(from, new Point(toX, toY), center, (int) from.distanceTo(center), clockwise);
        this.aperture = aperture;
    }

    public Arc getArc()
    {
        return arc;
    }

    @Override
    public Point getFrom()
    {
        return arc.getFrom();
    }

    @Override
    public Point getTo()
    {
        return arc.getTo();
    }

    @Override
    public void rotate(boolean clockwise)
    {
        arc = new Arc(arc.getFrom().rotateRelativeToOrigin(clockwise), arc.getTo().rotateRelativeToOrigin(clockwise),
                arc.getCenter().rotateRelativeToOrigin(clockwise), arc.getRadius(), arc.isClockwise());

        if (aperture != null)
            aperture = aperture.rotate(clockwise);
    }

    @Override
    public void move(Point point)
    {
        arc = new Arc(arc.getFrom().add(point), arc.getTo().add(point), arc.getCenter().add(point), arc.getRadius(), arc.isClockwise());
    }

    @Override
    public Point getMin()
    {
        int x, y;
        if (arc.containsAngle(-Math.PI))
            x = arc.getCenter().getX() - arc.getRadius();
        else
            x = Math.min(arc.getFrom().getX(), arc.getTo().getX());

        if (arc.containsAngle(-Math.PI / 2))
            y = arc.getCenter().getY() - arc.getRadius();
        else
            y = Math.min(arc.getFrom().getY(), arc.getTo().getY());

        x -= aperture.getWidth() / 2;
        y -= aperture.getHeight() / 2;

        return new Point(x, y);
    }
}
