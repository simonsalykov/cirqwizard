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

package org.cirqwizard.generator;

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBSize;
import org.cirqwizard.fx.State;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Point;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.post.Postprocessor;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.List;


public class TraceGCodeGenerator
{
    private Context context;
    private State state;
    private Settings settings;

    public TraceGCodeGenerator(Context context, State state, Settings settings)
    {
        this.context = context;
        this.state = state;
        this.settings = settings;
    }

    private RealNumber getX(RealNumber x)
    {
        return state == State.MILLING_BOTTOM_INSULATION ? x.negate() : x;
    }

    private List<Toolpath> getToolpaths()
    {
        return state == State.MILLING_TOP_INSULATION ? context.getTopTracesLayer().getToolpaths() : context.getBottomTracesLayer().getToolpaths();
    }

    public String generate(Postprocessor postprocessor, String xyFeed, String zFeed, String clearance, String safetyHeight,
                           String millingDepth, String spindleSpeed)
    {
        StringBuilder str = new StringBuilder();
        postprocessor.header(str);

        RealNumber g54X = new RealNumber(context.getG54X());
        if (state == State.MILLING_BOTTOM_INSULATION)
        {
            RealNumber laminateWidth = new RealNumber(context.getPcbSize() == PCBSize.Small ? settings.getMachineSmallPCBWidth() : settings.getMachineLargePCBWidth());
            RealNumber pinX = new RealNumber(settings.getMachineReferencePinX());
            g54X = pinX.multiply(2).add(laminateWidth).subtract(new RealNumber(context.getG54X()));
        }
        postprocessor.setupG54(str, g54X, new RealNumber(context.getG54Y()), new RealNumber(context.getG54Z()));
        postprocessor.selectWCS(str);

        RealNumber _clearance = new RealNumber(clearance);
        RealNumber _safetyHeight = new RealNumber(safetyHeight);
        RealNumber _millingDepth = new RealNumber(millingDepth);
        RealNumber _xyFeed = new RealNumber(xyFeed);
        RealNumber _zFeed = new RealNumber(zFeed);

        postprocessor.rapid(str, null, null, _clearance);

        postprocessor.spindleOn(str, spindleSpeed);
        Point prevLocation = null;
        for (Toolpath toolpath : getToolpaths())
        {
            if (!toolpath.isEnabled())
                continue;
            Curve curve = ((CuttingToolpath)toolpath).getCurve();
            if (prevLocation == null || !prevLocation.equals(curve.getFrom()))
            {
                postprocessor.rapid(str, null, null, _clearance);
                postprocessor.rapid(str, getX(curve.getFrom().getX()), curve.getFrom().getY(), _clearance);
                postprocessor.rapid(str, getX(curve.getFrom().getX()), curve.getFrom().getY(), _safetyHeight);
                postprocessor.linearInterpolation(str, getX(curve.getFrom().getX()), curve.getFrom().getY(),
                        _millingDepth, _zFeed);
            }
            if (toolpath instanceof LinearToolpath)
                postprocessor.linearInterpolation(str, getX(curve.getTo().getX()), curve.getTo().getY(), _millingDepth, _xyFeed);
            else if (toolpath instanceof CircularToolpath)
            {
                Arc arc = (Arc)curve;
                postprocessor.circularInterpolation(str, state == State.MILLING_BOTTOM_INSULATION ? !arc.isClockwise() : arc.isClockwise(),
                        getX(arc.getTo().getX()), arc.getTo().getY(), _millingDepth, getX(arc.getCenter().getX().subtract(arc.getFrom().getX())),
                        arc.getCenter().getY().subtract(arc.getFrom().getY()), _xyFeed);
            }
            prevLocation = curve.getTo();
        }
        postprocessor.rapid(str, null, null, _clearance);
        postprocessor.spindleOff(str);
        postprocessor.footer(str);

        return str.toString();
    }
}
