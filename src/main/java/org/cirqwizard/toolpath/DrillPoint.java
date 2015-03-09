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

import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Point;


public class DrillPoint extends CuttingToolpath
{
    private Point point;

    public DrillPoint(Point point, int diameter)
    {
        super((diameter + 50) / 100 * 100);    // Rounding to tenth of millimeter
        this.point = point;
    }

    @Override
    public Curve getCurve()
    {
        return null;
    }

    public Point getPoint()
    {
        return point;
    }

    public void setPoint(Point point)
    {
        this.point = point;
    }



}
