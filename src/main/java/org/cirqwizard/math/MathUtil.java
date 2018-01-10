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

package org.cirqwizard.math;

import org.cirqwizard.geom.Point;

public class MathUtil
{
    private static final double EPS = 1e-5;

    public static double bindAngle(double angle)
    {
        if (angle >= Math.PI)
            angle -= Math.PI * 2;
        if (angle < -Math.PI)
            angle += Math.PI * 2;
        return angle;
    }

    // To find orientation of point 'c' relative to the line segment (a, b)
    // Returns  0 if all three points are collinear.
    // Returns -1 if 'c' is clockwise to segment (a, b), i.e right of line formed by the segment.
    // Returns +1 if 'c' is counter clockwise to segment (a, b), i.e left of line
    // formed by the segment.
    private static int getOrientation(Point a, Point b, Point c)
    {
        double value = (b.getY() - a.getY()) * (c.getX() - b.getX()) -
                (b.getX() - a.getX()) * (c.getY() - b.getY());
        if (Math.abs(value) < (EPS)) return 0;
        return (value > 0) ? -1 : +1;
    }

    public static boolean pointOnLine(Point a, Point b, Point c)
    {
        return getOrientation(a, b, c) == 0 &&
                Math.min(a.getX(), b.getX()) <= c.getX() && c.getX() <= Math.max(a.getX(), b.getX()) &&
                Math.min(a.getY(), b.getY()) <= c.getY() && c.getY() <= Math.max(a.getY(), b.getY());
    }

    public static boolean segmentsIntersect(Point p1, Point p2, Point p3, Point p4)
    {
        // Get the orientation of points p3 and p4 in relation
        // to the line segment (p1, p2)
        int o1 = getOrientation(p1, p2, p3);
        int o2 = getOrientation(p1, p2, p4);
        int o3 = getOrientation(p3, p4, p1);
        int o4 = getOrientation(p3, p4, p2);

        // If the points p1, p2 are on opposite sides of the infinite
        // line formed by (p3, p4) and conversly p3, p4 are on opposite
        // sides of the infinite line formed by (p1, p2) then there is
        // an intersection.
        if (o1 != o2 && o3 != o4) return true;

        // Collinear special cases
        if (o1 == 0 && pointOnLine(p1, p2, p3)) return false;
        if (o2 == 0 && pointOnLine(p1, p2, p4)) return false;
        if (o3 == 0 && pointOnLine(p3, p4, p1)) return false;
        if (o4 == 0 && pointOnLine(p3, p4, p2)) return false;

        return false;
    }
}
