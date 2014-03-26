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

package org.cirqwizard.generation;

import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;

public class ToolpathPadsFilter
{
    private List<Toolpath> toolspaths;
    private List<GerberPrimitive> primitives;
    private int inflation;

    public ToolpathPadsFilter(List<Toolpath> toolspaths, List<GerberPrimitive> primitives, int inflation)
    {
        this.toolspaths = toolspaths;
        this.primitives = primitives;
        this.inflation = inflation;
    }

    public List<Toolpath> filter()
    {
        ArrayList<Toolpath> result = new ArrayList<>();
        for (Toolpath t : toolspaths)
        {
            if (!(t instanceof CuttingToolpath))
                continue;
            Point from = ((CuttingToolpath) t).getCurve().getFrom();
            Point to = ((CuttingToolpath) t).getCurve().getTo();
            boolean include = false;
            for (GerberPrimitive p : primitives)
            {
                if (p instanceof Flash)
                {
                    Flash flash = (Flash) p;
                    int threshold = flash.getAperture().getCircumRadius();
                    threshold += inflation + 10;
                    if (from.distanceTo(((Flash) p).getPoint()) < threshold && to.distanceTo(((Flash) p).getPoint()) < threshold)
                    {
                        include = true;
                        break;
                    }
                }
            }

            if (include)
                result.add(t);
        }

        return result;
    }
}
