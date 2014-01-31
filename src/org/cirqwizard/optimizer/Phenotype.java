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

package org.cirqwizard.optimizer;

import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.List;

public class Phenotype
{
    private List<Toolpath> toolpaths;
    private double feed;
    private double zFeed;
    private double clearance;
    private double safetyHeight;

    private final static double xRapids = (double)Settings.getXRapids() / Settings.RESOLUTION / 60;
    private final static double yRapids = (double)Settings.getYRapids() / Settings.RESOLUTION / 60;
    private final static double zRapids = (double)Settings.getZRapids() / Settings.RESOLUTION / 60;
    private final static double xRapidAcceleration = (double)Settings.getXRapidAcceleration() / Settings.RESOLUTION;
    private final static double yRapidAcceleration = (double)Settings.getYRapidAcceleration() / Settings.RESOLUTION;
    private final static double zRapidAcceleration = (double)Settings.getZRapidAcceleration() / Settings.RESOLUTION;
    private final static double feedAcceleration = (double) Settings.getFeedAcceleration() / Settings.RESOLUTION;
    private final static double arcFeed = (double) Settings.getArcFeed() / Settings.RESOLUTION / 60;

    public Phenotype(List<Toolpath> toolpaths, int feed, int zFeed, int clearance, int safetyHeight)
    {
        this.toolpaths = toolpaths;
        this.feed = (double)feed / Settings.RESOLUTION / 60;
        this.zFeed = (double) zFeed / Settings.RESOLUTION / 60;
        this.clearance = (double)clearance / Settings.RESOLUTION;
        this.safetyHeight = (double) safetyHeight / Settings.RESOLUTION;
    }

    public double calculateFitness()
    {
        Point currentLocation = new Point(0, 0);
        double totalTime = 0;

        double retractTime = calculatePathDuration(clearance, zRapids, zRapidAcceleration);
        double descentToSafetyHeight = calculatePathDuration(clearance - safetyHeight, zRapids, xRapidAcceleration);
        double finalDescent = calculatePathDuration(safetyHeight, zFeed, feedAcceleration);

        for (Toolpath t : toolpaths)
        {
            if (t instanceof CuttingToolpath)
            {
                Curve curve = ((CuttingToolpath) t).getCurve();
                if (!curve.getFrom().equals(currentLocation))
                {
                    double xRapidTime = calculatePathDuration((double)Math.abs(currentLocation.getX() - curve.getFrom().getX()) / Settings.RESOLUTION,
                            xRapids, xRapidAcceleration);
                    double yRapidTime = calculatePathDuration((double)Math.abs(currentLocation.getY() - curve.getFrom().getY()) / Settings.RESOLUTION,
                            yRapids, yRapidAcceleration);
                    totalTime += retractTime + Math.max(xRapidTime, yRapidTime) + descentToSafetyHeight + finalDescent;
                }

                if (curve instanceof Line)
                {
                    Line l = (Line) curve;
                    totalTime += calculatePathDuration(l.length() / 1000, feed, feedAcceleration);
                }
                else if (curve instanceof Arc)
                {
                    Arc arc = (Arc) curve;
                    totalTime += calculatePathDuration(arc.getCircumreference(), arcFeed, feedAcceleration);
                }
                currentLocation = curve.getTo();
            }
        }

        return totalTime;
    }

    private double calculatePathDuration(double length, double speed, double acceleration)
    {
        double accelerationDistance = speed * speed / (acceleration * 2);
        if (accelerationDistance * 2 < length)
        {
            return (length - accelerationDistance * 2) / speed + speed / acceleration * 2;
        }
        double topSpeed = Math.sqrt(acceleration * 2 * (length / 2));
        return (topSpeed / acceleration) * 2;
    }
}
