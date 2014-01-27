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

package org.cirqwizard.appertures.macro;

import org.cirqwizard.geom.Point;
import org.cirqwizard.math.RealNumber;

public class MacroCenterLine extends MacroPrimitive
{
    private RealNumber width;
    private RealNumber height;
    private Point center;

    public MacroCenterLine(RealNumber width, RealNumber height, Point center, RealNumber rotationAngle)
    {
        super(rotationAngle);
        this.width = width;
        this.height = height;
        this.center = center;
    }

    public RealNumber getWidth()
    {
        return width;
    }

    public RealNumber getHeight()
    {
        return height;
    }

    public Point getCenter()
    {
        return center;
    }

    public Point getFrom()
    {
        return translate(center.subtract(new Point(getWidth().divide(2), new RealNumber(0))));
    }

    public Point getTo()
    {
        return translate(center.add(new Point(getWidth().divide(2), new RealNumber(0))));
    }

    @Override
    public MacroPrimitive clone()
    {
        return new MacroCenterLine(width, height, center, getRotationAngle());
    }
}
