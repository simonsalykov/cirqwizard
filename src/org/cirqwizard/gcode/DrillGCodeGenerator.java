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
import org.cirqwizard.post.Postprocessor;
import org.cirqwizard.toolpath.DrillPoint;


public class DrillGCodeGenerator
{
    private Context context;

    public DrillGCodeGenerator(Context context)
    {
        this.context = context;
    }

    public String generate(Postprocessor postprocessor, int feed, int clearance, int safetyHeight,
                           int drillingDepth, int spindleSpeed)
    {
        StringBuilder str = new StringBuilder();
        postprocessor.header(str);

        postprocessor.setupG54(str, context.getG54X(), context.getG54Y(), context.getG54Z());
        postprocessor.selectWCS(str);

        postprocessor.rapid(str, null, null, clearance);
        postprocessor.spindleOn(str, spindleSpeed);
        for (DrillPoint drillPoint : context.getPcbLayout().getDrillingLayer().getToolpaths())
        {
            if (drillPoint.getToolDiameter() != context.getCurrentDrill() || !drillPoint.isEnabled())
                continue;
            postprocessor.rapid(str, drillPoint.getPoint().getX(), drillPoint.getPoint().getY(), clearance);
            postprocessor.rapid(str, null, null, safetyHeight);
            postprocessor.linearInterpolation(str, drillPoint.getPoint().getX(), drillPoint.getPoint().getY(), drillingDepth, feed);
            postprocessor.rapid(str, null, null, clearance);
        }
        postprocessor.spindleOff(str);

        return str.toString();
    }
}
