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
import org.cirqwizard.fx.PCBSize;
import org.cirqwizard.fx.State;
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Point;
import org.cirqwizard.post.Postprocessor;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.MachineSettings;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.List;


public class TraceGCodeGenerator
{
    private Context context;
    private State state;

    public TraceGCodeGenerator(Context context, State state)
    {
        this.context = context;
        this.state = state;
    }

    private int getX(int x)
    {
        return state == State.MILLING_BOTTOM_INSULATION ? -x : x;
    }

    private List<Toolpath> getToolpaths()
    {
        return state == State.MILLING_TOP_INSULATION ? context.getTopTracesLayer().getToolpaths() : context.getBottomTracesLayer().getToolpaths();
    }

    public String generate(Postprocessor postprocessor, int xyFeed, int zFeed, int arcFeed, int clearance, int safetyHeight,
                           int millingDepth, int spindleSpeed)
    {
        StringBuilder str = new StringBuilder();
        postprocessor.header(str);

        int g54X = context.getG54X();
        if (state == State.MILLING_BOTTOM_INSULATION)
        {
            MachineSettings machineSettings = SettingsFactory.getMachineSettings();
            int laminateWidth = context.getPcbSize() == PCBSize.Small ? machineSettings.getSmallPcbWidth().getValue() : machineSettings.getLargePcbWidth().getValue();
            int pinX = machineSettings.getReferencePinX().getValue();
            g54X = pinX * 2 + laminateWidth - context.getG54X();
        }
        postprocessor.setupG54(str, g54X, context.getG54Y(), context.getG54Z());
        postprocessor.selectWCS(str);

        postprocessor.rapid(str, null, null, clearance);

        postprocessor.spindleOn(str, spindleSpeed);
        Point prevLocation = null;
        for (Toolpath toolpath : getToolpaths())
        {
            if (!toolpath.isEnabled())
                continue;
            Curve curve = ((CuttingToolpath)toolpath).getCurve();
            if (prevLocation == null ||
                    Math.abs(prevLocation.getX() - curve.getFrom().getX()) > Settings.ROUNDING ||
                    Math.abs(prevLocation.getY() - curve.getFrom().getY()) > Settings.ROUNDING)
            {
                postprocessor.rapid(str, null, null, clearance);
                postprocessor.rapid(str, getX(curve.getFrom().getX()), curve.getFrom().getY(), clearance);
                postprocessor.rapid(str, getX(curve.getFrom().getX()), curve.getFrom().getY(), safetyHeight);
                postprocessor.linearInterpolation(str, getX(curve.getFrom().getX()), curve.getFrom().getY(),
                        millingDepth, zFeed);
            }
            if (toolpath instanceof LinearToolpath)
                postprocessor.linearInterpolation(str, getX(curve.getTo().getX()), curve.getTo().getY(), millingDepth, xyFeed);
            else if (toolpath instanceof CircularToolpath)
            {
                Arc arc = (Arc)curve;
                postprocessor.circularInterpolation(str, state == State.MILLING_BOTTOM_INSULATION ? !arc.isClockwise() : arc.isClockwise(),
                        getX(arc.getTo().getX()), arc.getTo().getY(), millingDepth, getX(arc.getCenter().getX() - arc.getFrom().getX()),
                        arc.getCenter().getY() - arc.getFrom().getY(), arcFeed);
            }
            prevLocation = curve.getTo();
        }
        postprocessor.rapid(str, null, null, clearance);
        postprocessor.spindleOff(str);
        postprocessor.footer(str);

        return str.toString();
    }
}
