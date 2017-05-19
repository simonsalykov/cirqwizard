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

import org.cirqwizard.generation.toolpath.DrillPoint;
import org.cirqwizard.post.Postprocessor;

import java.util.List;


public class DrillGCodeGenerator
{
    private int g54X;
    private int g54Y;
    private int g54Z;
    private List<DrillPoint> drillPoints;

    public DrillGCodeGenerator(int g54X, int g54Y, int g54Z, List<DrillPoint> drillPoints)
    {
        this.g54X = g54X;
        this.g54Y = g54Y;
        this.g54Z = g54Z;
        this.drillPoints = drillPoints;
    }

    public String generate(Postprocessor postprocessor, int feed, int clearance, int safetyHeight,
                           int drillingDepth, int spindleSpeed)
    {
        StringBuilder str = new StringBuilder();
        postprocessor.header(str);

        DrillPoint firstPoint = drillPoints.stream().filter(DrillPoint::isEnabled).findFirst().get();
        postprocessor.selectMachineWS(str);
        postprocessor.rapid(str, null, null, 0);
        postprocessor.rapid(str, g54X + firstPoint.getPoint().getX(), g54Y + firstPoint.getPoint().getY(), null);

        postprocessor.setupG54(str, g54X, g54Y, g54Z);
        postprocessor.selectWCS(str);

        postprocessor.rapid(str, null, null, clearance);
        postprocessor.spindleOn(str, spindleSpeed);
        drillPoints.stream().
                filter(DrillPoint::isEnabled).
                forEach(drillPoint ->
                {
                    postprocessor.rapid(str, drillPoint.getPoint().getX(), drillPoint.getPoint().getY(), clearance);
                    postprocessor.rapid(str, null, null, safetyHeight);
                    postprocessor.linearInterpolation(str, drillPoint.getPoint().getX(), drillPoint.getPoint().getY(), drillingDepth, feed);
                    postprocessor.rapid(str, null, null, clearance);
                });
        postprocessor.selectMachineWS(str);
        postprocessor.rapid(str, null, null, 0);
        postprocessor.spindleOff(str);

        return str.toString();
    }
}
