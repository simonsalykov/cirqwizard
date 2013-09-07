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

package org.cirqwizard.gerber;

import org.cirqwizard.geom.Point;
import org.cirqwizard.math.RealNumber;

import java.util.ArrayList;
import java.util.List;


public class GerberUtil
{
    public static List<GerberPrimitive> filterPrimitives(List<GerberPrimitive> elements, Point windowCenter, RealNumber radius)
    {
        ArrayList<GerberPrimitive> result = new ArrayList<GerberPrimitive>();
        for (GerberPrimitive p : elements)
        {
            boolean include = false;
            if (p instanceof Flash)
                include = ((Flash)p).getPoint().distanceTo(windowCenter).lessThan(radius);
            else if (p instanceof LinearShape)
                include = ((LinearShape)p).getFrom().distanceTo(windowCenter).lessThan(radius) ||
                        ((LinearShape)p).getTo().distanceTo(windowCenter).lessThan(radius);
            if (include)
                result.add(p);
        }

        return result;
    }
}
