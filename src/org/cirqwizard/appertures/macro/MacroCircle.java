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

public class MacroCircle extends MacroPrimitive
{
    private RealNumber diameter;
    private Point center;

    public MacroCircle(RealNumber diameter, Point center)
    {
        this.diameter = diameter;
        this.center = center;
    }

    public RealNumber getDiameter()
    {
        return diameter;
    }

    public Point getCenter()
    {
        return center;
    }
}
