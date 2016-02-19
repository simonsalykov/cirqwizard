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

import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Line;
import org.cirqwizard.generation.toolpath.CuttingToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.List;


public class GroupRenderer
{
    private static final String[] colors = new String[] {"Black", "Red", "Green"};

    public static String renderCurve(Curve curve)
    {
        StringBuffer str = new StringBuffer();
        if (curve instanceof Line)
        {
            Line line = (Line) curve;
            str.append("Line[{{").append(line.getFrom().getX()).append(',').
                    append(line.getFrom().getY()).append("},{").
                    append(line.getTo().getX()).append(',').
                    append(line.getTo().getY()).append("}}]");
        }
        else if (curve instanceof Arc)
        {
            Arc arc = (Arc) curve;
            str.append("Circle[{").append(arc.getCenter().getX()).append(",").
                    append(arc.getCenter().getY()).append("},").
                    append(arc.getRadius()).append(",{").
                    append(arc.getStart()).append(",").
                    append(arc.getEnd(false)).append("}]");
        }
        return str.toString();
    }

    public static String renderToolpaths(List<Toolpath> toolpaths)
    {
        StringBuilder str = new StringBuilder();
        str.append("Graphics[{");
        for (Toolpath toolpath : toolpaths)
            str.append(renderCurve(((CuttingToolpath)toolpath).getCurve())).append(",");
        str.setLength(str.length() - 1);
        str.append("},Axes->True]");
        return str.toString();
    }

}
