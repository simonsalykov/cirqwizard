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
    public static boolean polyIsCw(ArrayList<Point> p)
    {
        return VectorMath.vecDot(VectorMath.vecRot90CW(new Point(p.get(1).getX().subtract(p.get(0).getX()), p.get(1).getY().subtract(p.get(0).getY()))),
                new Point(p.get(2).getX().subtract(p.get(1).getX()), p.get(2).getY().subtract(p.get(1).getY()))).greaterOrEqualTo(0);
    }

    public static  ArrayList<Point> expandPolygon(ArrayList<Point> p, double distance)
    {
        ArrayList<Point> expanded = new ArrayList<Point>();
        Point d01, d12;

        for (int i = 0; i < p.size(); ++i)
        {
            Point pt0 = p.get(i > 0 ? i - 1 : p.size() - 1);
            Point pt1 = p.get(i);
            Point pt2 = p.get((i < p.size() - 1) ? i + 1 : 0);

            Point v01 = new Point(pt1.getX().subtract(pt0.getX()), pt1.getY().subtract(pt0.getY()));
            Point v12 = new Point(pt2.getX().subtract(pt1.getX()), pt2.getY().subtract(pt1.getY()));

            if (polyIsCw(p))
            {
                d01 = VectorMath.vecMul(VectorMath.vecUnit(VectorMath.vecRot90CCW(v01)), distance);
                d12 = VectorMath.vecMul(VectorMath.vecUnit(VectorMath.vecRot90CCW(v12)), distance);
            }
            else
            {
                d01 = VectorMath.vecMul(VectorMath.vecUnit(VectorMath.vecRot90CW(v01)), distance);
                d12 = VectorMath.vecMul(VectorMath.vecUnit(VectorMath.vecRot90CW(v12)), distance);
            }

            Line l1 = new Line(new Point(pt0.getX().add(d01.getX()), pt0.getY().add(d01.getY())),
                    new Point(pt1.getX().add(d01.getX()), pt1.getY().add(d01.getY())));
            Line l2 = new Line(new Point(pt1.getX().add(d12.getX()), pt1.getY().add(d12.getY())),
                    new Point(pt2.getX().add(d12.getX()), pt2.getY().add(d12.getY())));

            expanded.add(Line.intersectionPoint(l1, l2));
        }
        return expanded;
    }
}
