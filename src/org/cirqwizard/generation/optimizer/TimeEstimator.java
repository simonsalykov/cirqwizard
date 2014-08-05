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

package org.cirqwizard.generation.optimizer;

import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.List;

public class TimeEstimator
{
    private final static double xRapids = (double) ApplicationConstants.getXRapids() / ApplicationConstants.RESOLUTION / 60;
    private final static double yRapids = (double) ApplicationConstants.getYRapids() / ApplicationConstants.RESOLUTION / 60;
    private final static double zRapids = (double) ApplicationConstants.getZRapids() / ApplicationConstants.RESOLUTION / 60;
    private final static double xRapidAcceleration = (double) ApplicationConstants.getXRapidAcceleration() / ApplicationConstants.RESOLUTION;
    private final static double yRapidAcceleration = (double) ApplicationConstants.getYRapidAcceleration() / ApplicationConstants.RESOLUTION;
    private final static double zRapidAcceleration = (double) ApplicationConstants.getZRapidAcceleration() / ApplicationConstants.RESOLUTION;
    private final static double feedAcceleration = (double) ApplicationConstants.getFeedAcceleration() / ApplicationConstants.RESOLUTION;

    public static double calculateTotalDuration(List<Toolpath> toolpaths, double feed, double zFeed, double arcFeed, double clearance, double safetyHeight, boolean includeFeed,
                                                int mergeTolerance)
    {
        Point currentLocation = new Point(0, 0);
        double totalTime = 0;

        double retractTime = calculatePathDuration(clearance, zRapids, zRapidAcceleration);
        double descentToSafetyHeight = calculatePathDuration(clearance - safetyHeight, zRapids, xRapidAcceleration);
        double finalDescent = calculatePathDuration(safetyHeight, zFeed, feedAcceleration);

        if (toolpaths == null)
            return 0;

        for (Toolpath t : toolpaths)
        {
            if (t instanceof CuttingToolpath)
            {
                Curve curve = ((CuttingToolpath) t).getCurve();
                if (currentLocation.distanceTo(curve.getFrom()) > mergeTolerance)
                {
                    double xRapidTime = calculatePathDuration((double)Math.abs(currentLocation.getX() - curve.getFrom().getX()) / ApplicationConstants.RESOLUTION,
                            xRapids, xRapidAcceleration);
                    double yRapidTime = calculatePathDuration((double)Math.abs(currentLocation.getY() - curve.getFrom().getY()) / ApplicationConstants.RESOLUTION,
                            yRapids, yRapidAcceleration);
                    totalTime += retractTime + Math.max(xRapidTime, yRapidTime) + descentToSafetyHeight + finalDescent;
                }

                if (includeFeed)
                {
                    if (curve instanceof Line)
                    {
                        Line l = (Line) curve;
                        totalTime += calculatePathDuration(l.length() / ApplicationConstants.RESOLUTION, feed, feedAcceleration);
                    }
                    else if (curve instanceof Arc)
                    {
                        Arc arc = (Arc) curve;
                        totalTime += calculatePathDuration(arc.getCircumreference() / ApplicationConstants.RESOLUTION, arcFeed, feedAcceleration);
                    }
                }
                currentLocation = curve.getTo();
            }
        }

        return totalTime;
    }

    private static double calculatePathDuration(double length, double speed, double acceleration)
    {
        double accelerationDistance = speed * speed / (acceleration * 2);
        if (accelerationDistance * 2 < length)
            return (length - accelerationDistance * 2) / speed + speed / acceleration * 2;

        double topSpeed = Math.sqrt(acceleration * 2 * (length / 2));
        return (topSpeed / acceleration) * 2;
    }


}
