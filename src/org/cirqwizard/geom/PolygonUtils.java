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

package org.cirqwizard.geom;

import org.cirqwizard.math.VectorMath;

import java.util.ArrayList;


public class PolygonUtils
{
    public static boolean isPolygonClockwise(ArrayList<Point> p)
    {
        return VectorMath.dotProduct(VectorMath.rotateClockwise(p.get(1).subtract(p.get(0))), p.get(2).subtract(p.get(1))) >= 0;
    }

    public static  ArrayList<Point> expandPolygon(ArrayList<Point> p, double distance)
    {
        ArrayList<Point> expanded = new ArrayList<>();
        Point d01, d12;

        for (int i = 0; i < p.size(); ++i)
        {
            Point pt0 = p.get(i > 0 ? i - 1 : p.size() - 1);
            Point pt1 = p.get(i);
            Point pt2 = p.get((i < p.size() - 1) ? i + 1 : 0);

            Point v01 = pt1.subtract(pt0);
            Point v12 = pt2.subtract(pt1);

            if (isPolygonClockwise(p))
            {
                d01 = VectorMath.scalarMultiply(VectorMath.unitVector(VectorMath.rotateCounterclockwise(v01)), distance);
                d12 = VectorMath.scalarMultiply(VectorMath.unitVector(VectorMath.rotateCounterclockwise(v12)), distance);
            }
            else
            {
                d01 = VectorMath.scalarMultiply(VectorMath.unitVector(VectorMath.rotateClockwise(v01)), distance);
                d12 = VectorMath.scalarMultiply(VectorMath.unitVector(VectorMath.rotateClockwise(v12)), distance);
            }

            Line l1 = new Line(pt0.add(d01), pt1.add(d01));
            Line l2 = new Line(pt1.add(d12), pt2.add(d12));

            expanded.add(Line.intersectionPoint(l1, l2));
        }
        return expanded;
    }
}
