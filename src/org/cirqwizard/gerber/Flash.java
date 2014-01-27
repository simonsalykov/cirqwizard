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
import org.cirqwizard.geom.Point;
import org.cirqwizard.math.RealNumber;


public class Flash extends GerberPrimitive
{
    private Point point;

    public Flash(RealNumber x, RealNumber y, Aperture aperture)
    {
        point = new Point(x, y);
        this.aperture = aperture;
    }

    public Point getPoint()
    {
        return point;
    }

    public RealNumber getX()
    {
        return point.getX();
    }

    public RealNumber getY()
    {
        return point.getY();
    }

    @Override
    public void rotate(boolean clockwise)
    {
        if (clockwise)
            point = new Point(point.getY(), point.getX().negate());
        else
            point = new Point(point.getY().negate(), point.getX());
        aperture = aperture.rotate(clockwise);
    }

    @Override
    public void move(Point p)
    {
        point = point.add(p);
    }

    @Override
    public Point getMin()
    {
        return point;
    }

}
