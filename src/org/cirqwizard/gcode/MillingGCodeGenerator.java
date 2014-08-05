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

package org.cirqwizard.gcode;

import org.cirqwizard.fx.Context;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Point;
import org.cirqwizard.post.Postprocessor;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;


public class MillingGCodeGenerator
{
    private Context context;

    public MillingGCodeGenerator(Context context)
    {
        this.context = context;
    }

    public String generate(Postprocessor postprocessor, int xyFeed, int zFeed, int arcFeed, int clearance, int safetyHeight,
                           int millingDepth, int spindleSpeed)
    {
        StringBuilder str = new StringBuilder();
        postprocessor.header(str);

        postprocessor.setupG54(str, context.getG54X(), context.getG54Y(), context.getG54Z());
        postprocessor.selectWCS(str);

        postprocessor.rapid(str, null, null, clearance);
        postprocessor.spindleOn(str, spindleSpeed);
        Point prevLocation = null;
        for (Toolpath toolpath : context.getPcbLayout().getMillingLayer().getToolpaths())
        {
            if (!toolpath.isEnabled())
                continue;
            Curve curve = ((CuttingToolpath)toolpath).getCurve();
            if (prevLocation == null || !prevLocation.equals(curve.getFrom()))
            {
                postprocessor.rapid(str, null, null, clearance);
                postprocessor.rapid(str, curve.getFrom().getX(), curve.getFrom().getY(), clearance);
                postprocessor.rapid(str, null, null, safetyHeight);
                postprocessor.linearInterpolation(str, curve.getFrom().getX(), curve.getFrom().getY(),
                        millingDepth, zFeed);
            }
            if (toolpath instanceof LinearToolpath)
                postprocessor.linearInterpolation(str, curve.getTo().getX(), curve.getTo().getY(),
                        millingDepth, xyFeed);
            else if (toolpath instanceof CircularToolpath)
            {
                Arc arc = (Arc) curve;
                postprocessor.circularInterpolation(str, arc.isClockwise(), arc.getTo().getX(), arc.getTo().getY(),
                        millingDepth, arc.getCenter().getX() - arc.getFrom().getX(),
                        arc.getCenter().getY() - arc.getFrom().getY(), arcFeed);
            }
            prevLocation = curve.getTo();
        }
        postprocessor.rapid(str, null, null, clearance);
        postprocessor.spindleOff(str);

        return str.toString();
    }
}
