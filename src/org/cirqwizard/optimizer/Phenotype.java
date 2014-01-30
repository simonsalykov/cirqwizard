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

import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.settings.Settings;
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
            if (t instanceof LinearToolpath)
            {
                LinearToolpath lt = (LinearToolpath) t;
                if (lt.getCurve() instanceof Line)
                {
                    Line l = (Line) lt.getCurve();
                    if (!l.getFrom().equals(currentLocation))
                    {
                        double xRapidTime = calculatePathDuration((double)Math.abs(currentLocation.getX() - l.getFrom().getX()) / Settings.RESOLUTION,
                                xRapids, xRapidAcceleration);
                        double yRapidTime = calculatePathDuration((double)Math.abs(currentLocation.getY() - l.getFrom().getY()) / Settings.RESOLUTION,
                                yRapids, yRapidAcceleration);
                        totalTime += retractTime + Math.max(xRapidTime, yRapidTime) + descentToSafetyHeight + finalDescent;
                    }
                    totalTime += calculatePathDuration(l.length() / 1000, feed, feedAcceleration);
                    currentLocation = l.getTo();
                }
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
