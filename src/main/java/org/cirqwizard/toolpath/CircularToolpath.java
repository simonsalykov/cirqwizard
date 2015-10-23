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

package org.cirqwizard.toolpath;


import org.cirqwizard.geom.*;
import org.cirqwizard.geom.Point;


public class CircularToolpath extends CuttingToolpath
{
    private Arc arc;

    public CircularToolpath(int toolDiameter, Point from, Point to, Point center, int radius, boolean clockwise)
    {
        super(toolDiameter);
        arc = new Arc(from, to, center, radius, clockwise);
    }

    @Override
    public Curve getCurve()
    {
        return arc;
    }
}
