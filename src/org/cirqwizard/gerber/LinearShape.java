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
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.math.RealNumber;


public class LinearShape extends InterpolatingShape
{
    private Line line;

    public LinearShape(int fromX, int fromY, int toX, int toY, Aperture aperture)
    {
        Point from = new Point(fromX, fromY);
        Point to = new Point(toX, toY);
        line = new Line(from, to);
        this.aperture = aperture;
    }

    public Point getFrom()
    {
        return line.getFrom();
    }

    public Point getTo()
    {
        return line.getTo();
    }

    @Override
    public void rotate(boolean clockwise)
    {
        line.setFrom(line.getFrom().rotateRelativeToOrigin(clockwise));
        line.setTo(line.getTo().rotateRelativeToOrigin(clockwise));

        if (aperture != null)
            aperture = aperture.rotate(clockwise);
    }

    @Override
    public void move(Point point)
    {
        line.setFrom(line.getFrom().add(point));
        line.setTo(line.getTo().add(point));
    }

    @Override
    public Point getMin()
    {
        return new Point(line.getFrom().getX() < line.getTo().getX() ? line.getFrom().getX() : line.getTo().getX(),
                line.getFrom().getY() < line.getTo().getY() ? line.getFrom().getY() : line.getTo().getY());
    }

}
