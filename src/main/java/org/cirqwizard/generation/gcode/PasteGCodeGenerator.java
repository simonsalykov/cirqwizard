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

import org.cirqwizard.generation.toolpath.CuttingToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.post.Postprocessor;

import java.util.List;


public class PasteGCodeGenerator
{
    private int g54X;
    private int g54Y;
    private int g54Z;
    private List<Toolpath> toolpaths;

    public PasteGCodeGenerator(int g54X, int g54Y, int g54Z, List<Toolpath> toolpaths)
    {
        this.g54X = g54X;
        this.g54Y = g54Y;
        this.g54Z = g54Z;
        this.toolpaths = toolpaths;
    }

    public String generate(Postprocessor postprocessor, int preFeedPause, int postFeedPause, int feed, int clearance, int workingHeight)
    {
        StringBuilder str = new StringBuilder();
        postprocessor.header(str);

        postprocessor.selectMachineWS(str);
        postprocessor.rapid(str, null, null, 0);
        postprocessor.setupG54(str, g54X, g54Y, g54Z);
        postprocessor.selectWCS(str);

        boolean firstPad = true;
        for (Toolpath toolpath : toolpaths)
        {
            if (!toolpath.isEnabled())
                continue;
            Curve curve = ((CuttingToolpath)toolpath).getCurve();
            postprocessor.rapid(str, curve.getFrom().getX(), curve.getFrom().getY(), firstPad ? null : clearance);
            firstPad = false;
            postprocessor.rapid(str, null, null, workingHeight);
            postprocessor.syringeOn(str);
            postprocessor.pause(str, preFeedPause);
            postprocessor.linearInterpolation(str, curve.getTo().getX(), curve.getTo().getY(),
                    workingHeight, feed);
            postprocessor.syringeOff(str);
            postprocessor.pause(str, postFeedPause);
            postprocessor.rapid(str, null, null, clearance);
        }
        postprocessor.selectMachineWS(str);
        postprocessor.rapid(str, null, null, 0);
        postprocessor.footer(str);

        return str.toString();
    }
}
