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
    private int width;
    private int height;
    private Point center;

    public MacroCenterLine(int width, int height, Point center, int rotationAngle)
    {
        super(rotationAngle);
        this.width = width;
        this.height = height;
        this.center = center;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public Point getCenter()
    {
        return center;
    }

    public Point getFrom()
    {
        return translate(center.subtract(new Point(width / 2, 0)));
    }

    public Point getTo()
    {
        return translate(center.add(new Point(width / 2, 0)));
    }

    @Override
    public MacroPrimitive clone()
    {
        return new MacroCenterLine(width, height, center, getRotationAngle());
    }
}
