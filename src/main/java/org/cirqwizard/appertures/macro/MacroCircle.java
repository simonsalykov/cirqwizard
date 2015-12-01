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

public class MacroCircle extends MacroPrimitive
{
    private int diameter;
    private Point center;

    public MacroCircle(int diameter, Point center)
    {
        this(diameter, center, 0);
    }

    public MacroCircle(int diameter, Point center, int rotationAngle)
    {
        super(rotationAngle);
        this.diameter = diameter;
        this.center = center;
    }

    public int getDiameter()
    {
        return diameter;
    }

    public Point getCenter()
    {
        return translate(center);
    }

    @Override
    public MacroPrimitive clone()
    {
        return new MacroCircle(diameter, center, getRotationAngle());
    }
}
