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

package org.cirqwizard.generation.gcode;

import org.cirqwizard.fx.Context;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Point;
import org.cirqwizard.layers.Board;
import org.cirqwizard.post.Postprocessor;
import org.cirqwizard.generation.toolpath.CircularToolpath;
import org.cirqwizard.generation.toolpath.CuttingToolpath;
import org.cirqwizard.generation.toolpath.LinearToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.settings.ApplicationConstants;

import java.util.List;


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

        List<Toolpath> toolpaths = context.getPanel().getToolspaths(Board.LayerType.MILLING);
        Toolpath firstToolpath = toolpaths.stream().filter(Toolpath::isEnabled).findFirst().get();
        Curve firstCurve = ((CuttingToolpath)firstToolpath).getCurve();
        postprocessor.selectMachineWS(str);
        postprocessor.rapid(str, null, null, 0);
        postprocessor.rapid(str, context.getG54X() + firstCurve.getFrom().getX(), context.getG54Y() + firstCurve.getFrom().getY(), null);

        postprocessor.setupG54(str, context.getG54X(), context.getG54Y(), context.getG54Z());
        postprocessor.selectWCS(str);

        postprocessor.rapid(str, null, null, clearance);
        postprocessor.spindleOn(str, spindleSpeed);
        Point prevLocation = null;
        for (Toolpath toolpath : toolpaths)
        {
            if (!toolpath.isEnabled())
                continue;
            Curve curve = ((CuttingToolpath)toolpath).getCurve();
            if (prevLocation == null ||
                    Math.abs(prevLocation.getX() - curve.getFrom().getX()) > ApplicationConstants.ROUNDING ||
                    Math.abs(prevLocation.getY() - curve.getFrom().getY()) > ApplicationConstants.ROUNDING)
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
        postprocessor.selectMachineWS(str);
        postprocessor.rapid(str, null, null, 0);
        postprocessor.spindleOff(str);

        return str.toString();
    }
}
