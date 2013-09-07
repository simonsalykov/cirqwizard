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

import org.cirqwizard.geom.*;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.render.Segment;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.Toolpath;

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
            str.append("Line[{{").append(line.getFrom().getX().toString()).append(',').
                    append(line.getFrom().getY().toString()).append("},{").
                    append(line.getTo().getX().toString()).append(',').
                    append(line.getTo().getY().toString()).append("}}]");
        }
        else if (curve instanceof Arc)
        {
            Arc arc = (Arc) curve;
            str.append("Circle[{").append(arc.getCenter().getX().toString()).append(",").
                    append(arc.getCenter().getY().toString()).append("},").
                    append(arc.getRadius().toString()).append(",{").
                    append(arc.getStart().toString()).append(",").
                    append(arc.getEnd(false).toString()).append("}]");
        }
        return str.toString();
    }

    public static String renderPrimitives(List<GerberPrimitive> elements, RealNumber offset)
    {
        StringBuilder str = new StringBuilder();
//        str.append("Graphics[{");
//        for (GerberPrimitive p : elements)
//            str.append(renderNakedCycle(p.getSubdivision(offset).getOuterFace().getInnerComponents().get(0))).append(",");
//        str.setLength(str.length() - 1);
//        str.append("},Axes->True]");
        return str.toString();
    }

    public static String renderPrimitivesWindow(List<GerberPrimitive> elements, RealNumber offset, Point windowCenter, RealNumber radius)
    {
        StringBuilder str = new StringBuilder();
        return renderPrimitives(GerberUtil.filterPrimitives(elements, windowCenter, radius), offset);
    }

    public static String renderSegments(List<Segment> segments)
    {
        StringBuilder str = new StringBuilder();
        str.append("Graphics[{");
        for (Segment segment : segments)
            str.append("Line[{{").append(segment.getStart().x).append(",").append(segment.getStart().y).append("},{").
                    append(segment.getEnd().x).append(",").append(segment.getEnd().y).append("}}],");
        str.setLength(str.length() - 1);
        str.append("},Axes->True]");
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
