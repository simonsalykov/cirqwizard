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
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.post.Postprocessor;
import org.cirqwizard.toolpath.DrillPoint;


public class DrillGCodeGenerator
{
    private Context context;

    public DrillGCodeGenerator(Context context)
    {
        this.context = context;
    }

    private RealNumber getSelectedDrillDiameter()
    {
        return context.getDrillingLayer().getDrillDiameters().get(context.getCurrentDrill());
    }

    public String generate(Postprocessor postprocessor, String feed, String clearance, String safetyHeight,
                           String drillingDepth, String spindleSpeed)
    {
        StringBuilder str = new StringBuilder();
        postprocessor.header(str);

        postprocessor.setupG54(str, new RealNumber(context.getG54X()), new RealNumber(context.getG54Y()),
                new RealNumber(context.getG54Z()));
        postprocessor.selectWCS(str);

        RealNumber _clearance = new RealNumber(clearance);
        RealNumber _safetyHeight = new RealNumber(safetyHeight);
        RealNumber _drillingDepth = new RealNumber(drillingDepth);
        RealNumber _feed = new RealNumber(feed);

        postprocessor.rapid(str, null, null, _clearance);
        postprocessor.spindleOn(str, spindleSpeed);
        RealNumber selectedDrill = getSelectedDrillDiameter();
        for (DrillPoint drillPoint : context.getDrillingLayer().getToolpaths())
        {
            if (!drillPoint.getToolDiameter().equals(selectedDrill) || !drillPoint.isEnabled())
                continue;
            postprocessor.rapid(str, drillPoint.getPoint().getX(), drillPoint.getPoint().getY(), _clearance);
            postprocessor.rapid(str, null, null, _safetyHeight);
            postprocessor.linearInterpolation(str, drillPoint.getPoint().getX(), drillPoint.getPoint().getY(), _drillingDepth, _feed);
            postprocessor.rapid(str, null, null, _clearance);
        }
        postprocessor.spindleOff(str);

        return str.toString();
    }
}
