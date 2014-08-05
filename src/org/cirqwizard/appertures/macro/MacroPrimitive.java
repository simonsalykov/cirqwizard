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

public abstract class MacroPrimitive
{
    private int rotationAngle;

    protected MacroPrimitive()
    {
        this(0);
    }

    protected MacroPrimitive(int rotationAngle)
    {
        this.rotationAngle = rotationAngle;
    }

    protected Point translate(Point p)
    {
        if (rotationAngle == 0)
            return p;
        double theta = Math.toRadians(rotationAngle);
        double x = Math.cos(theta) * p.getX() - Math.sin(theta) * p.getY();
        double y = Math.sin(theta) * p.getX() + Math.cos(theta) * p.getY();
        return new Point((int) x, (int) y);
    }

    public int getRotationAngle()
    {
        return rotationAngle;
    }

    public void setRotationAngle(int rotationAngle)
    {
        this.rotationAngle = rotationAngle;
    }

    public abstract MacroPrimitive clone();
}
